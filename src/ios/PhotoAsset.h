//
//  PhotoAsset.h
//  ZetBook
//

#import "AssetIdentifier.h"

@protocol PhotoAsset <NSObject>

- (AssetIdentifier *)getIdentifier;
- (UIImage *)getImage;
- (UIImage *)getThumbnail;

@end
