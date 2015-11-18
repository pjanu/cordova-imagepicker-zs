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
        NSPredicate *predicate = [NSPredicate predicateWithFormat:@"name=%@", @"id"];
        NSURLComponents *query = [NSURLComponents componentsWithURL:asset.defaultRepresentation.url resolvingAgainstBaseURL:NO];
        self.identifier = [[query.queryItems filteredArrayUsingPredicate:predicate] firstObject].value;
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
