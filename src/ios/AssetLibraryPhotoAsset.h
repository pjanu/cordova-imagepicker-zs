//
//  AssetLibraryPhotoAsset.h
//  ZetBook
//

#import "PhotoAsset.h"
#import <AssetsLibrary/AssetsLibrary.h>

@interface AssetLibraryPhotoAsset : NSObject<PhotoAsset>

@property (nonatomic, strong) ALAsset *asset;
@property (nonatomic, assign) BOOL selected;

- (id)initWithAsset:(ALAsset *)asset;
- (ALAsset*)getAsset;

@end
