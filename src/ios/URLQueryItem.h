//
//  URLQueryItem.h
//  ZetBook
//

@interface URLQueryItem : NSObject

@property NSString *name;
@property NSString *value;

- (id)initWithString:(NSString *)token;
+ (id)urlQueryItemFromString:(NSString *)token;

@end