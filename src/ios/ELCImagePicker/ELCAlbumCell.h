//
//  ELCAlbumCell.h
//  ZetBook
//

#import <UIKit/UIKit.h>

@interface ELCAlbumCell : UITableViewCell

- (void)setImageSize:(CGSize)imageSize textPadding:(CGFloat)textPadding;

@property (nonatomic, assign) CGSize imageSize;
@property (nonatomic, assign) CGFloat textPadding;

@end