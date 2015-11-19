//
//  AssetIdentifier.m
//  ZetBook
//

#import "AssetIdentifier.h"

@implementation AssetIdentifier

-(id)initWithAsset:(ALAsset *)asset {
    self = [super init];

    if(self)
    {
        NSURL *url = asset.defaultRepresentation.url;
        NSURLComponents *query = [NSURLComponents componentsWithURL:url resolvingAgainstBaseURL:NO];
        NSPredicate *predicate = [NSPredicate predicateWithFormat:@"name=%@", @"id"];
        self.identifier = [[query.queryItems filteredArrayUsingPredicate:predicate] firstObject].value;
        self.url = [url absoluteString];
    }

    return self;
}

-(BOOL)isEqualWithIdentifier:(AssetIdentifier *)other {
    return self.identifier == other.identifier;
}

-(NSString *)description {
    return [NSString stringWithFormat:@"Id: %@", self.identifier];
}

@end
