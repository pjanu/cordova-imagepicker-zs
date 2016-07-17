//
//  PhotoAsset.h
//  ZetBook
//

#import "AssetIdentifier.h"
#import <CoreLocation/CoreLocation.h>

@protocol PhotoAsset <NSObject>

- (AssetIdentifier *)getIdentifier;
- (UIImage *)getImage;
- (UIImage *)getThumbnail;
- (NSString *)getAssetType;
- (CGSize)getOriginalSize;
- (UIImageOrientation)getOrientation;
- (NSString *)getExifDate;
- (CLLocation *)getLocation;

@end
