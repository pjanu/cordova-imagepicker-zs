//
//  LocalizedString.h
//  ZetBook
//

@interface LocalizedString : NSObject

@property NSDictionary *dictionary;

+ (NSDictionary *)dictionary;
+ (NSString *)get:(NSString *)key;

@end
