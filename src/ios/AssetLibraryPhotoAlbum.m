//
//  AssetLibraryPhotoAlbum.m
//  ZetBook
//

#import "AssetLibraryPhotoAlbum.h"

@implementation AssetLibraryPhotoAlbum

- (id)init:(ALAssetsGroup *)assetGroup
{
    self = [super init];
    if(self)
    {
        self.assetGroup = assetGroup;
    }
    return self;
}

- (NSUInteger)getCount
{
    __block NSInteger count = 0;

    [self.assetGroup enumerateAssetsUsingBlock:^(ALAsset *asset, NSUInteger index, BOOL *stop) {
        if(asset.defaultRepresentation)
            count++;
    }];

    return count;
}

- (NSArray *)getPhotos
{
    return [[NSArray alloc] init];
}

- (NSString *)getTitle
{
    return [self.assetGroup valueForProperty:ALAssetsGroupPropertyName];
}

- (UIImage *)getThumbnail
{
    return [UIImage imageWithCGImage:[self.assetGroup posterImage]];
}

- (ALAssetsGroup *)getAssetGroup
{
    return self.assetGroup;
}

@end
