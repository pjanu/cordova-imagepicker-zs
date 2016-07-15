//
//  AssetLibraryPhotoLibrary.h
//  ZetBook
//

#import "PhotoLibrary.h"
#import <AssetsLibrary/AssetsLibrary.h>

@interface AssetLibraryPhotoLibrary : NSObject<PhotoLibrary>

@property (nonatomic, strong) ALAssetsLibrary *library;

- (id)init:(ALAssetsLibrary *)library;

@end