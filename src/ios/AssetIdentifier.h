//
//  AssetIdentifier.h
//  ZetBook
//

#import <AssetsLibrary/AssetsLibrary.h>

@interface AssetIdentifier : NSObject

@property NSString *identifier;

-(id)initWithAsset:(ALAsset *)asset;

-(BOOL)isEqualWithIdentifier:(AssetIdentifier *)identifier;

@end
