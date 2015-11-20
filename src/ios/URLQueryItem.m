//
//  URLQueryItem.m
//  ZetBook
//

#import "URLQueryItem.h"

@implementation URLQueryItem

- (id)initWithString:(NSString *)token {
    self = [super init];

    if(self)
    {
        NSArray *pair = [token componentsSeparatedByString:@"="];
        self.name = pair.firstObject;
        self.value = pair.lastObject;
    }

    return self;
}

+ (id)urlQueryItemFromString:(NSString *)token {
    return [[URLQueryItem alloc] initWithString:token];
};

@end
