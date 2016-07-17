//
//  SOSPicker.h
//  SyncOnSet
//
//  Created by Christopher Sullivan on 10/25/13.
//
//

#import <Cordova/CDVPlugin.h>
#import "ELCAlbumPickerController.h"
#import "ELCImagePickerController.h"
#import "PhotoAsset.h"

@interface SOSPicker : CDVPlugin <ELCImagePickerControllerDelegate, UINavigationControllerDelegate, UIScrollViewDelegate>

@property (copy)   NSString* callbackId;

- (void) getPictures:(CDVInvokedUrlCommand *)command;
- (UIImage*)imageByScalingNotCroppingForSize:(UIImage*)anImage toSize:(CGSize)frameSize;
- (UIImage*)resizeToFile:(ALAsset *)asset file:(NSString *)filePath toSize:(CGSize)targetSize;
- (UIImage *)originalToFile:(ALAsset *)asset file:(NSString *)filePath;
- (NSDictionary *)formatResult:(NSMutableArray *)files state:(NSString *)resultState;
- (bool)isPortraitImage:(NSObject<PhotoAsset> *)asset;
- (UIColor*)colorFromNumber:(NSInteger)color;

@property (nonatomic, assign) NSInteger width;
@property (nonatomic, assign) NSInteger height;
@property (nonatomic, assign) NSInteger quality;
@property (nonatomic, strong) ALAssetsLibrary *library;

@end
