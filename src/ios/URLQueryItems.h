//
//  URLQueryItems.h
//  ZetBook
//

#import "URLQueryItem.h"

@interface URLQueryItems : NSObject

@property NSArray *items;

- (id)initWithQueryString:(NSString *)query;
+ (id)itemsWithQueryString:(NSString *)query;
- (NSString *)valueForParameter:(NSString *)parameter;

@end
