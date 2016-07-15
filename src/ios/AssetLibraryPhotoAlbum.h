//
//  AssetLibraryPhotoAlbum.h
//  ZetBook
//

#import "PhotoAlbum.h"
#import <AssetsLibrary/AssetsLibrary.h>

@interface AssetLibraryPhotoAlbum : NSObject<PhotoAlbum>

@property (strong) ALAssetsGroup *assetGroup;

- (id)init:(ALAssetsGroup*)assetGroup;

- (ALAssetsGroup*)getAssetGroup;

@end