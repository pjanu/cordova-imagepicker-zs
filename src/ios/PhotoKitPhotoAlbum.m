//
//  PhotoKitPhotoAlbum.m
//  ZetBook
//

#import "PhotoKitPhotoAlbum.h"
#import "PhotoKitPhotoAsset.h"

@implementation PhotoKitPhotoAlbum

- (id)initWithCollection:(PHAssetCollection *)collection
{
    self = [super init];
    if(self)
    {
        self.collection = collection;
        self.manager = [PHImageManager defaultManager];
        self.count = [[self getPhotos] count];
    }
    return self;
}

- (NSString *)getTitle
{
    return [self.collection localizedTitle];
}

- (NSUInteger)getCount
{
    return self.count;
}

- (NSMutableArray *)getPhotos
{
    __block NSMutableArray *photos = [[NSMutableArray alloc] init];

    PHFetchResult *fetch = [PHAsset fetchAssetsInAssetCollection:self.collection options:[self getFetchOptions]];

    [fetch enumerateObjectsUsingBlock:^(PHAsset *asset, NSUInteger index, BOOL *stop){
        NSObject<PhotoAsset> *photoAsset = [[PhotoKitPhotoAsset alloc] initWithAsset:asset];
        [photos addObject:photoAsset];
    }];

    return photos;
}

- (void)fetchThumbnail:(ThumbnailFetch)onThumbnailFetch
{
    if(self.thumbnail)
    {
        onThumbnailFetch(self.thumbnail);
        return;
    }

    PHFetchResult *fetch = [PHAsset fetchAssetsInAssetCollection:self.collection options:[self getFetchOptions]];

    [self.manager requestImageForAsset:[fetch lastObject]
                        targetSize:CGSizeMake(160.0f, 160.0f)
                      contentMode:PHImageContentModeAspectFill
                          options:nil
                    resultHandler:^(UIImage *image, NSDictionary *info){
                        self.thumbnail = image;
                        onThumbnailFetch(self.thumbnail);
                    }];
}

- (PHFetchOptions *)getFetchOptions
{
    PHFetchOptions *options = [[PHFetchOptions alloc] init];
    [options setPredicate:[NSPredicate predicateWithFormat:@"mediaType = %d", PHAssetMediaTypeImage]];
    return options;
}

@end
