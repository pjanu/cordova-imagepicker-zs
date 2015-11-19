//
//  PhotoAttributes.h
//  ZetBook
//

@interface PhotoAttributes : NSObject

@property NSString *largePhotoName;
@property NSString *thumbnailName;
@property NSString *miniPhotoName;
@property NSNumber *originalPhotoWidth;
@property NSNumber *originalPhotoHeight;
@property NSNumber *finalWidth;
@property NSNumber *finalHeight;
@property NSString *originalFilePath;

-(id)init;
-(id)initWithFilePath:(NSString *)filePath;
-(NSDictionary *)toDictionary;
-(NSString *)toJSONString;

@end