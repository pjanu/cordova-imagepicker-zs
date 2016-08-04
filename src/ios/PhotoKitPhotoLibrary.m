//
//  PhotoKitPhotoLibrary.m
//  ZetBook
//

#import "PhotoKitPhotoLibrary.h"
#import "PhotoKitPhotoAlbum.h"
#import "PhotoKitPhotoAsset.h"
#import "LocalizedString.h"

@implementation PhotoKitPhotoLibrary

-(id)init
{
    self = [super init];
    return self;
}

- (NSMutableArray *)fetchAlbums:(AlbumFetch)onAlbumFetch
{
    __block NSMutableArray *albums = [[NSMutableArray alloc] init];

    void (^fillAlbums)() = ^()
    {
        @autoreleasepool
        {
            void (^addCollection)(PHAssetCollection*, NSUInteger, BOOL*) =
            ^(PHAssetCollection *collection, NSUInteger index, BOOL *stop)
            {

                NSObject<PhotoAlbum> *album = [[PhotoKitPhotoAlbum alloc]
                                               initWithCollection:collection];
                [albums addObject:album];
                onAlbumFetch();
            };

            PHFetchResult *result = [PHAssetCollection
                fetchAssetCollectionsWithType:PHAssetCollectionTypeAlbum
                                      subtype:PHAssetCollectionSubtypeAny
                                      options:nil];

            [result enumerateObjectsUsingBlock:addCollection];
            addCollection([self getAllPhotosAlbum], 0, nil);
        }
    };

    dispatch_async(dispatch_get_main_queue(), fillAlbums);

    return albums;
}

- (NSMutableDictionary *)getSelectedPhotos:(NSArray *)identifiers
{
    NSMutableDictionary *selected = [[NSMutableDictionary alloc] init];
    PHFetchResult *fetch = [PHAsset fetchAssetsWithLocalIdentifiers:identifiers options:nil];
    [fetch enumerateObjectsUsingBlock:^(PHAsset *asset, NSUInteger index, BOOL *stop){
        selected[identifiers[index]] = [[PhotoKitPhotoAsset alloc] initWithAsset:asset];
    }];
    return selected;
}

- (PHAssetCollection *)getAllPhotosAlbum
{
    PHFetchResult *allPhotos = [PHAsset fetchAssetsWithOptions:nil];
    return [PHAssetCollection transientAssetCollectionWithAssetFetchResult:allPhotos title:@"All Photos"];
}

@end
