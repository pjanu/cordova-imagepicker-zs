//
// PhotoKitPhotoAsset.h
// ZetBook
//

#import "PhotoAsset.h"
#import <Photos/Photos.h>

@interface PhotoKitPhotoAsset : NSObject<PhotoAsset>

- (id)initWithAsset:(PHAsset *)asset;

@property (nonatomic, strong) PHAsset *asset;
@property (nonatomic, strong) UIImage *thumbnail;
@property (nonatomic, strong) UIImage *image;

@end
