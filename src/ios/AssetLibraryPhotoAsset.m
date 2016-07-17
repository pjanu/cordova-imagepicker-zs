//
//  AssetLibraryPhotoAsset.m
//  ZetBook
//

#import "AssetLibraryPhotoAsset.h"

@implementation AssetLibraryPhotoAsset

- (id)initWithAsset:(ALAsset *)asset
{
    self = [super init];
    if(self)
    {
        self.asset = asset;
    }
    return self;
}

- (AssetIdentifier *)getIdentifier
{
    return [[AssetIdentifier alloc] initWithAsset:self.asset];
}

- (UIImage *)getImage
{
    ALAssetRepresentation *assetRep = [self.asset defaultRepresentation];
    UIImageOrientation orientation = (UIImageOrientation) [assetRep orientation];
    return [self getImageWithOrientation:orientation];
}

- (UIImage *)getImageWithOrientation:(UIImageOrientation)orientation
{
    ALAssetRepresentation *assetRep = [self.asset defaultRepresentation];
    CGImageRef imgRef = [assetRep fullScreenImage];
    return [UIImage imageWithCGImage:imgRef scale:1.0f orientation:orientation];
}

- (UIImage *)getThumbnail
{
    return [UIImage imageWithCGImage:[self.asset thumbnail]];
}

- (NSString *)getAssetType
{
    return [self.asset valueForProperty:ALAssetPropertyType];
}

- (CGSize)getOriginalSize
{
    return [[self.asset defaultRepresentation] dimensions];
}

- (UIImageOrientation)getOrientation;
{
    return [[self.asset valueForProperty:ALAssetPropertyOrientation] intValue];
}

- (NSString *)getExifDate
{
    return [self.asset valueForProperty:ALAssetPropertyDate];
}

- (CLLocation *)getLocation
{
    return [self.asset valueForProperty:ALAssetPropertyLocation];
}

- (ALAsset *)getAsset
{
    return self.asset;
}

@end
