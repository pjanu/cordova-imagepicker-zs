//
//  AssetCell.m
//
//  Created by ELC on 2/15/11.
//  Copyright 2011 ELC Technologies. All rights reserved.
//

#import "ELCAssetCell.h"
#import "ELCAsset.h"
#import "ELCAssetTablePicker.h"

@interface ELCAssetCell ()

@property (nonatomic, strong) NSArray *rowAssets;
@property (nonatomic, strong) NSMutableArray *imageViewArray;
@property (nonatomic, strong) NSMutableArray *overlayViewArray;
@property (nonatomic, assign) int padding;

@end

@implementation ELCAssetCell

//Using auto synthesizers

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:UITableViewCellStyleDefault reuseIdentifier:reuseIdentifier];
    if (self) {
        UITapGestureRecognizer *tapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(cellTapped:)];
        [self addGestureRecognizer:tapRecognizer];

        NSMutableArray *mutableArray = [[NSMutableArray alloc] initWithCapacity:4];
        self.imageViewArray = mutableArray;

        NSMutableArray *overlayArray = [[NSMutableArray alloc] initWithCapacity:4];
        self.overlayViewArray = overlayArray;

        self.padding = 4;
    }
    return self;
}

- (void)setAssets:(NSArray *)assets
{
    int size = [self getCellSize];
    self.rowAssets = assets;
    for (UIImageView *view in _imageViewArray) {
        [view removeFromSuperview];
    }
    for (UIImageView *view in _overlayViewArray) {
        [view removeFromSuperview];
    }

    for (int i = 0; i < [_rowAssets count]; ++i) {

        ELCAsset *elcAsset = [_rowAssets objectAtIndex:i];

        if (i < [_imageViewArray count]) {
            UIImageView *imageView = [_imageViewArray objectAtIndex:i];
            imageView.image = [elcAsset.asset getThumbnail];
        } else {
            UIImageView *imageView = [[UIImageView alloc] initWithImage:[elcAsset.asset getThumbnail]];
            [_imageViewArray addObject:imageView];
        }

        if (i < [_overlayViewArray count]) {
            UIView *overlayView = [_overlayViewArray objectAtIndex:i];
            overlayView.hidden = elcAsset.selected ? NO : YES;
        } else {
            UIView *overlayView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, size, size)];
            UIColor *overlayColor = [[(id) elcAsset.parent overlayColor] colorWithAlphaComponent:0.75f];
            [overlayView setBackgroundColor:overlayColor];
            [_overlayViewArray addObject:overlayView];
            overlayView.hidden = elcAsset.selected ? NO : YES;
        }
    }
}

- (void)cellTapped:(UITapGestureRecognizer *)tapRecognizer
{
    int size = [self getCellSize];
    CGPoint point = [tapRecognizer locationInView:self];
    CGFloat totalWidth = self.rowAssets.count * size + (self.rowAssets.count - 1) * self.padding;
    CGFloat startX = (self.bounds.size.width - totalWidth) / 2;

    CGRect frame = CGRectMake(startX, self.padding / 2, size, size);

    for (int i = 0; i < [_rowAssets count]; ++i) {
        if (CGRectContainsPoint(frame, point)) {
            ELCAsset *asset = [_rowAssets objectAtIndex:i];
            asset.selected = !asset.selected;
            UIImageView *overlayView = [_overlayViewArray objectAtIndex:i];
            overlayView.hidden = !asset.selected;
            [(id) asset.parent updateSelectedCount];
            [(id) asset.parent updateTitleView];
            break;
        }
        frame.origin.x = frame.origin.x + frame.size.width + self.padding;
    }
}

- (void)layoutSubviews
{
    int size = [self getCellSize];
    CGFloat totalWidth = self.rowAssets.count * size + (self.rowAssets.count - 1) * self.padding;
    CGFloat startX = (self.bounds.size.width - totalWidth) / 2;

    CGRect frame = CGRectMake(startX, self.padding / 2, size, size);

    for (int i = 0; i < [_rowAssets count]; ++i) {
        UIImageView *imageView = [_imageViewArray objectAtIndex:i];
        [imageView setFrame:frame];
        [self addSubview:imageView];

        UIImageView *overlayView = [_overlayViewArray objectAtIndex:i];
        [overlayView setFrame:frame];
        [self addSubview:overlayView];

        frame.origin.x = frame.origin.x + frame.size.width + self.padding;
    }
}

- (int)getCellSize {
    return self.width - self.padding;
}


@end
