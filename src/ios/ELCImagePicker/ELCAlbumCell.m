//
//  ELCAlbumCell.m
//  ZetBook
//

#import "ELCAlbumCell.h"

@implementation ELCAlbumCell

- (void)layoutSubviews
{
    [super layoutSubviews];
    CGRect rectangle = CGRectMake((self.textPadding - self.imageSize.width) * 0.5, 0, self.imageSize.width, self.imageSize.height);
    [self.imageView setBounds:rectangle];
    [self.imageView setFrame:rectangle];
    self.imageView.contentMode = UIViewContentModeScaleAspectFill;
    [self.imageView setClipsToBounds:YES];
    [self.textLabel setFrame:CGRectMake(self.textPadding, 0, self.contentView.frame.size.width - self.textPadding, self.imageSize.height)];
}

- (void)setImageSize:(CGSize)imageSize textPadding:(CGFloat)textPadding
{
    self.imageSize = imageSize;
    self.textPadding = textPadding;
}

@end