//
//  AssetPickerTitleStyle.h
//  ZetBook
//

@interface AssetPickerTitleStyle : NSObject

@property (nonatomic, weak) NSString *placeholderString;

-(id)initWithStyle:(NSString *)style;
+(id)titleStyleWithStyle:(NSString *)style;

-(NSString *)getPlaceholderString;
-(void)setStyle:(NSString *)style;

@end