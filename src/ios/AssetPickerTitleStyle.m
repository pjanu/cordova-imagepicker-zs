//
//  AssetPickerTitleStyle.m
//  ZetBook
//

#import "AssetPickerTitleStyle.h"

@implementation AssetPickerTitleStyle

const NSString *SHORT_STYLE = @"selectedOnly";
const NSString *NUMBER_STYLE = @"numberOnly";

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
    if ([SHORT_STYLE isEqualToString:style]) {
        self.placeholderString = @"Selected %d";
    }
    else if ([NUMBER_STYLE isEqualToString:style]) {
        self.placeholderString = @"%d";
    }
    else {
        self.placeholderString = @"Selected %d of %d";
    }
}

@end