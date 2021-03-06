//
//  PhotoAsset.h
//  ZetBook
//

#import "AssetIdentifier.h"
#import <CoreLocation/CoreLocation.h>

@protocol PhotoAsset <NSObject>

- (AssetIdentifier *)getIdentifier;
- (UIImage *)getImage;
- (UIImage *)getImageWithOrientation:(UIImageOrientation)orientation;
- (UIImage *)getThumbnail:(CGSize)size;
- (NSString *)getAssetType;
- (CGSize)getOriginalSize;
- (UIImageOrientation)getOrientation;
- (NSString *)getExifDate;
- (CLLocation *)getLocation;

@end
