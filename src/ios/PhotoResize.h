//
//  PhotoResize.h
//  ZetBook
//

@interface PhotoResize : NSObject

@property CGSize size;

- (id) initWithCGSize:(CGSize)size;

+ (id) resizeWithCGSize:(CGSize)size;

+ (id) resizeForThumbnail;

+ (id) resizeForMini;

@end
