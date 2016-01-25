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
@property NSString *exifDate;
@property NSNumber *exifLatitude;
@property NSNumber *exifLongitude;

-(id)init;
-(id)initWithFilePath:(NSString *)filePath;
-(NSDictionary *)toDictionary;
-(NSString *)toJSONString;
-(void)swapOriginalDimensions;

@end