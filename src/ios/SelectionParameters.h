//
//  SelectionParameters.h
//  ZetBook
//

@interface SelectionParameters : NSObject

@property (nonatomic, assign) NSInteger maximumPhotoCount;
@property (nonatomic, assign) NSInteger addPhotoCount;
@property (nonatomic, strong) NSArray *selected;

@end