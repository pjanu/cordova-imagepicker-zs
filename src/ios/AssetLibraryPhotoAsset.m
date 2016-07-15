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

- (AssetIdentifier*)getIdentifier
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

@end
