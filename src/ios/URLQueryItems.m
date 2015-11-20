//
//  URLQueryItems.m
//  ZetBook
//

#import "URLQueryItems.h"

@implementation URLQueryItems

- (id)initWithQueryString:(NSString *)query {
    self = [super init];

    if(self)
    {
        NSMutableArray *queryItems = [[NSMutableArray alloc] init];

        for (NSString *component in [query componentsSeparatedByString:@"&"]) {
            [queryItems addObject:[URLQueryItem urlQueryItemFromString:component]];
        }

        self.items = [NSArray arrayWithArray:queryItems];
    }

    return self;
}

+ (id)itemsWithQueryString:(NSString *)query {
    return [[URLQueryItems alloc] initWithQueryString:query];
}

- (NSString *)valueForParameter:(NSString *)parameter {
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"name=%@", parameter];
    return [[self.items filteredArrayUsingPredicate:predicate].firstObject valueForKey:@"value"];
}

@end
