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
    return nil;
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
