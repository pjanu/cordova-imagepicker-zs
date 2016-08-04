//
//  PhotoKitPhotoAlbum.h
//  ZetBook
//

#import "PhotoAlbum.h"
#import <Photos/Photos.h>

@interface PhotoKitPhotoAlbum : NSObject<PhotoAlbum>

- (id)initWithCollection:(PHAssetCollection *)collection;

@property (nonatomic, strong) PHAssetCollection *collection;
@property (nonatomic, strong) UIImage *thumbnail;
@property (nonatomic, strong) PHImageManager *manager;
@property (nonatomic, assign) NSUInteger count;

@end
