//
// PhotoKitPhotoAsset.m
// ZetBook
//

#import "PhotoKitPhotoAsset.h"
#import "AssetIdentifier.h"

@implementation PhotoKitPhotoAsset

- (id)initWithAsset:(PHAsset *)asset
{
    self = [super init];
    if(self)
    {
        self.asset = asset;
        self.thumbnail = nil;
        self.image = nil;
    }
    return self;
}

- (AssetIdentifier *)getIdentifier;
{
    AssetIdentifier *identifier = [[AssetIdentifier alloc] init];
    identifier.identifier = [self.asset localIdentifier];
    identifier.url = [self.asset localIdentifier];
    return identifier;
}

- (UIImage *)getImage;
{
    if(self.image == nil)
    {
        self.image = [[UIImage alloc] init];
        PHImageRequestOptions *options = [[PHImageRequestOptions alloc] init];
        options.synchronous = YES;
        PHImageManager *manager = [PHImageManager defaultManager];
        [manager requestImageForAsset:self.asset targetSize:PHImageManagerMaximumSize contentMode:PHImageContentModeDefault options:options resultHandler:^(UIImage *image, NSDictionary *info){
            self.image = image;
        }];
    }

    return self.image;
}

- (UIImage *)getImageWithOrientation:(UIImageOrientation)orientation;
{
    return [self getImage];
}

- (UIImage *)getThumbnail:(CGSize)size;
{
    if(self.thumbnail == nil)
    {
        self.thumbnail = [[UIImage alloc] init];
        PHImageRequestOptions *options = [[PHImageRequestOptions alloc] init];
        options.synchronous = YES;
        PHImageManager *manager = [PHImageManager defaultManager];
        [manager requestImageForAsset:self.asset
                           targetSize:size
                          contentMode:PHImageContentModeAspectFill
                              options:options
                        resultHandler:^(UIImage *image, NSDictionary *info){
                            self.thumbnail = image;
                        }];
    }

    return self.thumbnail;
}

- (NSString *)getAssetType;
{
    return [self.asset mediaType] == PHAssetMediaTypeImage ? @"image" : nil;
}

- (CGSize)getOriginalSize;
{
    return CGSizeMake([self.asset pixelWidth], [self.asset pixelHeight]);
}

- (UIImageOrientation)getOrientation;
{
    return [[self getImage] imageOrientation];
}

- (NSString *)getExifDate;
{
    return [[self.asset creationDate] description];
}

- (CLLocation *)getLocation;
{
    return [self.asset location];
}

@end
