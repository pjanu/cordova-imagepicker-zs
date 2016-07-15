//
//  AssetLibraryPhotoAlbum.m
//  ZetBook
//

#import "AssetLibraryPhotoAlbum.h"
#import "ELCAsset.h"

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

- (NSMutableArray *)getPhotos
{
    __block NSMutableArray *photos = [[NSMutableArray alloc] init];

    [self.assetGroup enumerateAssetsUsingBlock:^(ALAsset *asset, NSUInteger index, BOOL *stop){
        if(asset == nil)
        {
            return;
        }

        if(asset.defaultRepresentation)
        {
            [photos addObject:[[ELCAsset alloc] initWithAsset:asset]];
        }
    }];

    return photos;
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
