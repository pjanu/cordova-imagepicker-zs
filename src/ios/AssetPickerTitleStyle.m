//
//  AssetPickerTitleStyle.m
//  ZetBook
//

#import "AssetPickerTitleStyle.h"

@implementation AssetPickerTitleStyle

const NSString *SHORT_STYLE = @"selectedOnly";

-(id)initWithStyle:(NSString *)style {
    self = [super init];
    if(self)
    {
        [self setStyle:style];
    }
    return self;
}

+(id)titleStyleWithStyle:(NSString *)style {
    return [[AssetPickerTitleStyle alloc] initWithStyle:style];
}

-(NSString *)getPlaceholderString {
    return self.placeholderString;
}

-(void)setStyle:(NSString *)style {
    self.placeholderString = [SHORT_STYLE isEqual:style] ? @"Selected %d" : @"Selected %d of %d";
}

@end