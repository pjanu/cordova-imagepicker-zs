//
//  AssetIdentifier.h
//  ZetBook
//

#import <AssetsLibrary/AssetsLibrary.h>

@interface AssetIdentifier : NSObject

@property NSString *identifier;
@property NSString *url;

-(id)initWithAsset:(ALAsset *)asset;

-(BOOL)isEqualWithIdentifier:(AssetIdentifier *)other;

@end
