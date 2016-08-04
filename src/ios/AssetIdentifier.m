//
//  AssetIdentifier.m
//  ZetBook
//

#import "AssetIdentifier.h"
#import "URLQueryItems.h"

@implementation AssetIdentifier

-(id)initWithAsset:(ALAsset *)asset {
    self = [super init];

    if(self)
    {
        NSURL *url = [asset valueForProperty:ALAssetPropertyAssetURL];
        NSURLComponents *query = [NSURLComponents componentsWithURL:url resolvingAgainstBaseURL:NO];
        self.identifier = [[URLQueryItems itemsWithQueryString:query.query] valueForParameter:@"id"];
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
