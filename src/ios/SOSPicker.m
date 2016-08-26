//
//  SOSPicker.m
//  SyncOnSet
//
//  Created by Christopher Sullivan on 10/25/13.
//
//

#import "SOSPicker.h"
#import "ELCAlbumPickerController.h"
#import "ELCImagePickerController.h"
#import "ELCAssetTablePicker.h"
#import "PhotoAttributes.h"
#import "PhotoResize.h"
#import "AssetIdentifier.h"
#import "AssetPickerTitleStyle.h"
#import "InterfaceOrientation.h"
#import <CoreLocation/CoreLocation.h>

#define CDV_PHOTO_PREFIX @"cdv_photo_"

@implementation SOSPicker

@synthesize callbackId;

- (void) getPictures:(CDVInvokedUrlCommand *)command {
    NSDictionary *options = [command.arguments objectAtIndex: 0];

    NSInteger maximumImagesCount = [[options objectForKey:@"maximumImagesCount"] integerValue];
    NSInteger addImagesCount = [[options objectForKey:@"addImagesCount"] integerValue];
    NSInteger selectedColor = [[options objectForKey:@"selectedColor"] integerValue];
    NSArray *selected = [[options objectForKey:@"selected"] componentsSeparatedByString:@";"];
    NSString *titleStyle = [options objectForKey:@"titleStyle"];
    NSString *orientation = [options objectForKey:@"orientation"];
    BOOL simpleHeader = [[options objectForKey:@"simpleHeader"] boolValue];
    BOOL countOkEval = [[options objectForKey:@"countOkEval"] boolValue];
	self.width = [[options objectForKey:@"width"] integerValue];
	self.height = [[options objectForKey:@"height"] integerValue];
	self.quality = [[options objectForKey:@"quality"] integerValue];

    self.library = [[ALAssetsLibrary alloc] init];

    if (simpleHeader) {
        titleStyle = @"numberOnly";
    }

    NSMutableDictionary *selectedImages = [[NSMutableDictionary alloc] init];
    for (NSString *identifier in selected) {
        if ([identifier isEqualToString:@""]) {
            continue;
        }

        [self.library assetForURL:[NSURL URLWithString:identifier] resultBlock:^(ALAsset *asset) {
            selectedImages[identifier] = asset;
        } failureBlock:^(NSError *error){}];
    }

	// Create the an album controller and image picker
	ELCAlbumPickerController *albumController = [[ELCAlbumPickerController alloc] init];

	if (maximumImagesCount == 1) {
      albumController.immediateReturn = true;
      albumController.singleSelection = true;
   } else {
      albumController.immediateReturn = false;
      albumController.singleSelection = false;
   }

    ELCImagePickerController *imagePicker = [[ELCImagePickerController alloc] initWithRootViewController:albumController];
    imagePicker.limitedOrientation = [InterfaceOrientation interfaceOrientationWithOrientation:orientation];
    imagePicker.titleStyle = [AssetPickerTitleStyle titleStyleWithStyle:titleStyle];
    imagePicker.maximumImagesCount = maximumImagesCount + addImagesCount;
    imagePicker.addImagesCount = addImagesCount;
    imagePicker.selected = selected;
    imagePicker.returnsOriginalImage = 1;
    imagePicker.imagePickerDelegate = self;
    imagePicker.simpleHeader = simpleHeader;
    imagePicker.countOkEval = countOkEval;
    imagePicker.overlayColor = [self colorFromNumber:selectedColor];

    albumController.selectedImages = selectedImages;
    albumController.library = self.library;
    albumController.parent = imagePicker;
	self.callbackId = command.callbackId;
	// Present modally
	[self.viewController presentViewController:imagePicker
	                       animated:YES
	                     completion:nil];
}


- (void)elcImagePickerController:(ELCImagePickerController *)picker didFinishPickingMediaWithInfo:(NSArray *)info {
	CDVPluginResult* result = nil;
    NSMutableArray *addedFiles = [[NSMutableArray alloc] init];
    NSString* docsPath = [NSTemporaryDirectory()stringByStandardizingPath];
    NSFileManager* fileMgr = [[NSFileManager alloc] init];
    NSString* filePath;
    ALAsset* asset = nil;
    CGSize targetSize = CGSizeMake(self.width, self.height);
    
    // prevent app from sleep, dont forge to turn it back on
    [UIApplication sharedApplication].idleTimerDisabled = YES;

    for (NSDictionary *dict in info) {
        asset = [dict objectForKey:@"ALAsset"];
        // From ELCImagePickerController.m

        @autoreleasepool {

            PhotoAttributes *attributes = [[PhotoAttributes alloc] init];

            ALAssetRepresentation *assetRep = [asset defaultRepresentation];
            CGSize originalSize = [assetRep dimensions];

            NSDictionary *resizes = [NSDictionary dictionaryWithObjectsAndKeys:[PhotoResize resizeWithCGSize:originalSize], @"originalPhotoName",
                                            [PhotoResize resizeWithCGSize:targetSize], @"largePhotoName",
                                            [PhotoResize resizeForThumbnail], @"thumbnailName",
                                            [PhotoResize resizeForMini], @"miniPhotoName",
                                            nil];

            NSMutableDictionary *images = [[NSMutableDictionary alloc] init];

            for (NSString *key in resizes) {

                @try
                {
                    PhotoResize *resize = [resizes objectForKey:key];
                    filePath = [self getFilePath:fileMgr inDir:docsPath size:resize.size];

                    if(self.width == 0 && self.height == 0)
                    {
                        [images setValue:[self originalToFile:asset file:filePath] forKey:key];
                    }
                    else
                    {
                        [images setValue:[self resizeToFile:asset file:filePath toSize:resize.size] forKey:key];
                    }

                    [attributes setValue:[[NSURL fileURLWithPath:filePath] absoluteString] forKey:key];
                }
                @catch (NSException *exception)
                {
                    result = [CDVPluginResult resultWithStatus:CDVCommandStatus_IO_EXCEPTION messageAsString:[exception name]];
                    break;
                }
            }

            attributes.originalFilePath = [[AssetIdentifier alloc] initWithAsset:asset].url;
            attributes.originalPhotoWidth = [NSNumber numberWithFloat:originalSize.width];
            attributes.originalPhotoHeight = [NSNumber numberWithFloat:originalSize.height];

            if([self isPortraitImage:asset])
            {
                [attributes swapOriginalDimensions];
            }

            UIImage *largeImage = [images objectForKey:@"largePhotoName"];
            attributes.finalWidth = [NSNumber numberWithInteger:largeImage.size.width];
            attributes.finalHeight = [NSNumber numberWithInteger:largeImage.size.height];

            attributes.exifDate = [[asset valueForProperty:ALAssetPropertyDate] description];
            CLLocation *location = [asset valueForProperty:ALAssetPropertyLocation];
            attributes.exifLatitude = [NSNumber numberWithDouble:location.coordinate.latitude];
            attributes.exifLongitude = [NSNumber numberWithDouble:location.coordinate.longitude];

            [addedFiles addObject:[attributes toJSONString]];
        }
	}

    if (nil == result) {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:[self formatResult:addedFiles state:@"ok"]];
    }
    [UIApplication sharedApplication].idleTimerDisabled = NO;
	[self.viewController dismissViewControllerAnimated:YES completion:nil];
	[self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
}

- (NSDictionary *)formatResult:(NSMutableArray *)files state:(NSString *)resultState {
    return [NSDictionary dictionaryWithObjectsAndKeys:files, @"addedFiles",
                         resultState, @"state", nil];
}

- (bool)isPortraitImage:(ALAsset *)asset {
    UIImageOrientation orientation = [[asset valueForProperty:ALAssetPropertyOrientation] intValue];
    return orientation == UIImageOrientationLeft || orientation == UIImageOrientationRight || orientation == UIImageOrientationLeftMirrored || orientation == UIImageOrientationRight;
}

- (void)elcImagePickerControllerDidCancel:(ELCImagePickerController *)picker {
	[self.viewController dismissViewControllerAnimated:YES completion:nil];
	CDVPluginResult* pluginResult = nil;
    NSMutableArray *emptyArray = [NSMutableArray array];
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:[self formatResult:emptyArray state:@"cancelled"]];
	[self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
}

- (UIColor *)colorFromNumber:(NSInteger)color {
    Byte alpha = (color & 0xFF000000) >> 24;
    Byte red = (color & 0x00FF0000) >> 16;
    Byte green = (color & 0x0000FF00) >> 8;
    Byte blue = (color & 0x000000FF) >> 0;
    return [UIColor colorWithRed:red/255.0 green:green/255.0 blue:blue/255.0 alpha:alpha/255.0];
}

- (NSString *)getFilePath:(NSFileManager *)fileMgr inDir:(NSString *)directory size:(CGSize)size {
    NSString *filePath;

    NSString *resize = [NSString stringWithFormat:@"%dx%d_", (int)size.width, (int)size.height];

    int i = 1;
    do {
        filePath = [NSString stringWithFormat:@"%@/%@%@%03d.%@", directory, CDV_PHOTO_PREFIX, resize, i++, @"jpg"];
    } while ([fileMgr fileExistsAtPath:filePath]);

    return filePath;
}

- (UIImage *)resizeToFile:(ALAsset *)asset file:(NSString *)filePath toSize:(CGSize)targetSize {
    @autoreleasepool {
        UIImageOrientation orientation = UIImageOrientationUp;
        ALAssetRepresentation *assetRep = [asset defaultRepresentation];
        CGImageRef imgRef = [assetRep fullScreenImage];

        UIImage *image = [UIImage imageWithCGImage:imgRef scale:1.0f orientation:orientation];
        image = [self imageByScalingNotCroppingForSize:image toSize:targetSize];
        NSData *data = UIImageJPEGRepresentation(image, self.quality/100.0f);
        NSError *err;

        if (![data writeToFile:filePath options:NSAtomicWrite error:&err]) {
            NSException *exception = [NSException exceptionWithName:[err localizedDescription] reason:nil userInfo:nil];
            @throw exception;
        };

        return image;
    }
}

- (UIImage *)originalToFile:(ALAsset *)asset file:(NSString *)filePath {
    ALAssetRepresentation *assetRep = [asset defaultRepresentation];
    CGImageRef imgRef = [assetRep fullResolutionImage];
    UIImageOrientation orientation = (UIImageOrientation) [assetRep orientation];
    UIImage *image = [UIImage imageWithCGImage:imgRef scale:1.0f orientation:orientation];
    NSData *data = UIImageJPEGRepresentation(image, self.quality/100.0f);
    NSError *err;
    if (![data writeToFile:filePath options:NSAtomicWrite error:&err]) {
        NSException *exception = [NSException exceptionWithName:[err localizedDescription] reason:nil userInfo:nil];
        @throw exception;
    }

    return image;
}

- (UIImage*)imageByScalingNotCroppingForSize:(UIImage*)anImage toSize:(CGSize)frameSize
{
    UIImage* sourceImage = anImage;
    UIImage* newImage = nil;
    CGSize imageSize = sourceImage.size;
    CGFloat width = imageSize.width;
    CGFloat height = imageSize.height;
    CGFloat targetWidth = frameSize.width;
    CGFloat targetHeight = frameSize.height;
    CGFloat scaleFactor = 0.0;
    CGSize scaledSize = frameSize;

    if (CGSizeEqualToSize(imageSize, frameSize) == NO) {
        CGFloat widthFactor = targetWidth / width;
        CGFloat heightFactor = targetHeight / height;

        // opposite comparison to imageByScalingAndCroppingForSize in order to contain the image within the given bounds
        if (widthFactor == 0.0) {
            scaleFactor = heightFactor;
        } else if (heightFactor == 0.0) {
            scaleFactor = widthFactor;
        } else if (widthFactor > heightFactor) {
            scaleFactor = heightFactor; // scale to fit height
        } else {
            scaleFactor = widthFactor; // scale to fit width
        }
        scaledSize = CGSizeMake(width * scaleFactor, height * scaleFactor);
    }

    UIGraphicsBeginImageContext(scaledSize); // this will resize

    [sourceImage drawInRect:CGRectMake(0, 0, scaledSize.width, scaledSize.height)];

    newImage = UIGraphicsGetImageFromCurrentImageContext();
    if (newImage == nil) {
        NSLog(@"could not scale image");
    }

    // pop the context to get back to the default
    UIGraphicsEndImageContext();
    return newImage;
}

@end
