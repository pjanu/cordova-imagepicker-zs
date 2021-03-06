//
//  ELCAssetTablePicker.m
//
//  Created by ELC on 2/15/11.
//  Copyright 2011 ELC Technologies. All rights reserved.
//

#import "ELCAssetTablePicker.h"
#import "ELCAssetCell.h"
#import "ELCAsset.h"
#import "ELCAlbumPickerController.h"
#import "AssetIdentifier.h"
#import "LocalizedString.h"
#import <UIKit/UIKit.h>

@implementation ELCAssetTablePicker

//Using auto synthesizers

- (id)init
{
    self = [super init];
    if (self) {
        //Sets a reasonable default bigger then 0 for columns
        //So that we don't have a divide by 0 scenario
        self.columns = 4;
    }
    return self;
}

- (void)viewDidLoad
{
    [self.tableView setSeparatorStyle:UITableViewCellSeparatorStyleNone];
    [self.tableView setAllowsSelection:NO];

    self.cellWidth = [self calculateWidthOfCell];

    self.titleView = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, self.simpleHeader ? 30 : self.view.bounds.size.width, 30)];
    [self.titleView setBackgroundColor:[self getTitleViewBackground]];
    [self.titleView setTextAlignment:NSTextAlignmentCenter];

    NSMutableArray *tempArray = [[NSMutableArray alloc] init];
    self.elcAssets = tempArray;

    if (self.immediateReturn) {

    } else {
        UIBarButtonItem *doneButtonItem = [[UIBarButtonItem alloc] initWithTitle:[LocalizedString get:@"Done"] style:UIBarButtonItemStyleDone target:self action:@selector(doneAction:)];
        [self.navigationItem setRightBarButtonItem:doneButtonItem];
        [self setTitle:[LocalizedString get:@"Loading..."]];

        [self.navigationItem setTitleView:self.titleView];
    }

    [self performSelectorInBackground:@selector(preparePhotos) withObject:nil];
}

- (int) calculateCountOfColumns
{
    return self.view.bounds.size.width / self.cellWidth;
}

- (int) calculateWidthOfCell
{
    return ceil([UIScreen mainScreen].bounds.size.width * 0.25);
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    self.columns = [self calculateCountOfColumns];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    self.columns = [self calculateCountOfColumns];
    [self.tableView reloadData];
    self.spinner = [[Spinner alloc] init:UIActivityIndicatorViewStyleWhiteLarge withSize:60.0 withBackgroundColor:[UIColor blackColor]];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];

    if (![[self.navigationController viewControllers] containsObject:self]) {
        // We were removed from the navigation controller's view controller stack
        // thus, we can infer that the back button was pressed
        [self backAction];
    }
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return [self.limitedOrientation getMask];
}

- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
    [super didRotateFromInterfaceOrientation:fromInterfaceOrientation];
    self.columns = [self calculateCountOfColumns];
    [self.tableView reloadData];
}

- (void)preparePhotos
{
    @autoreleasepool {

        for (NSObject<PhotoAsset> *photoAsset in [self.album getPhotos])
        {
            ELCAsset *elcAsset = [[ELCAsset alloc] initWithAsset:photoAsset];
            [elcAsset setParent:self];
            NSString *identifier = [[photoAsset getIdentifier] url];
            BOOL isSelected = [[self.selectedImages allKeys] containsObject:identifier];
            [elcAsset setSelected:isSelected];
            [self.elcAssets addObject:elcAsset];
        }

        dispatch_sync(dispatch_get_main_queue(), ^{
            [self.tableView reloadData];
            // scroll to bottom
            long section = [self numberOfSectionsInTableView:self.tableView] - 1;
            long row = [self tableView:self.tableView numberOfRowsInSection:section] - 1;
            if(section >= 0 && row >= 0)
            {
                NSIndexPath *ip = [NSIndexPath indexPathForRow:row
                                               inSection:section];
                [self.tableView scrollToRowAtIndexPath:ip
                                atScrollPosition:UITableViewScrollPositionBottom
                                animated:NO];
            }

            NSString *title = self.singleSelection ? [LocalizedString get:@"Pick Photo"] : [self getSelectedCountTitle];
            [self setTitle:title];
            [self updateTitleView];
        });
    }
}

- (NSInteger)getCountOfSelectedPhotos {
    return self.selection.addPhotoCount + (int) [self totalSelectedAssets];
}

- (void)updateSelectedCount {
    [self setTitle:[self getSelectedCountTitle]];
}

- (void)updateTitleView {
    [self.titleView setBackgroundColor:[self getTitleViewBackground]];
}

- (UIColor *)getTitleViewBackground {
    NSInteger count = [self getCountOfSelectedPhotos];
    BOOL ok = self.countOkEval && count >= 12 && count <= 60 && count % 4 == 0;
    UIColor *green = [UIColor colorWithRed:0.0f green:0.8f blue:0.2f alpha:0.9f];
    return ok ? green : [UIColor clearColor];
}

- (NSString *)getSelectedCountTitle {
    NSString *placeholder = [self.titleStyle getPlaceholderString];
    return [NSString stringWithFormat:[LocalizedString get:placeholder], [self getCountOfSelectedPhotos], self.selection.maximumPhotoCount];
}

- (void)setTitle:(NSString *)title {
    [self.titleView setText:title];
}

- (NSInteger)findAsset:(NSMutableArray *)selectedAssetsImages withIdentifier:(NSString *)identifier
{
    NSInteger index = 0;
    for (NSObject<PhotoAsset> *asset in selectedAssetsImages)
    {
        if([identifier isEqualToString:[[asset getIdentifier] url]])
        {
            break;
        }
        index++;
    }
    return index;
}

- (NSMutableArray *)getSelectedImages
{
    NSMutableArray *selectedAssetsImages = [[NSMutableArray alloc] initWithArray:[self.selectedImages allValues]];

    for(ELCAsset *elcAsset in self.elcAssets)
    {
        NSString *identifier = [[elcAsset.asset getIdentifier] url];
        BOOL isSelected = [elcAsset selected];
        BOOL alreadySelected = [[self.selectedImages allKeys] containsObject:identifier];

        if(isSelected && !alreadySelected)
        {
            [selectedAssetsImages addObject:elcAsset.asset];
        }
        else if(!isSelected && alreadySelected)
        {
            NSInteger index = [self findAsset:selectedAssetsImages withIdentifier:identifier];
            [selectedAssetsImages removeObjectAtIndex:index];
        }
    }

    return selectedAssetsImages;
}

- (NSMutableDictionary *)getSelectedImagesMap
{
    NSMutableDictionary *selectedImages = [[NSMutableDictionary alloc] init];

    for (NSObject<PhotoAsset> *asset in [self getSelectedImages]) {
        NSString *identifier = [[asset getIdentifier] url];
        selectedImages[identifier] = asset;
    }

    return selectedImages;
}

- (void)backAction
{
    NSMutableDictionary *map = [self getSelectedImagesMap];
    [self.selectedImages removeAllObjects];
    for (NSString *identifier in map) {
        self.selectedImages[identifier] = [map objectForKey:identifier];
    }
}

- (void)doneAction:(id)sender
{
    [self.spinner show];
    [self.parent selectedAssets:[self getSelectedImages]];
    [self.spinner hide];
}

- (BOOL)shouldSelectAsset:(ELCAsset *)asset
{
    NSUInteger selectionCount = self.selection.addPhotoCount;
    for (ELCAsset *elcAsset in self.elcAssets) {
        if (elcAsset.selected) selectionCount++;
    }
    BOOL shouldSelect = YES;
    if ([self.parent respondsToSelector:@selector(shouldSelectAsset:previousCount:)]) {
        shouldSelect = [self.parent shouldSelectAsset:asset previousCount:selectionCount];
    }
    return shouldSelect;
}

- (void)assetSelected:(ELCAsset *)asset
{
    if (self.singleSelection) {

        for (ELCAsset *elcAsset in self.elcAssets) {
            if (asset != elcAsset) {
                elcAsset.selected = NO;
            }
        }
    }
    if (self.immediateReturn) {
        NSArray *singleAssetArray = @[asset.asset];
        [(NSObject *)self.parent performSelector:@selector(selectedAssets:) withObject:singleAssetArray afterDelay:0];
    }
}

#pragma mark UITableViewDataSource Delegate Methods

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    // Return the number of sections.
    return 1;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if (self.columns <= 0) { //Sometimes called before we know how many columns we have
        self.columns = 4;
    }
    NSInteger numRows = ceil([self.elcAssets count] / (float)self.columns);
    return numRows;
}

- (NSArray *)assetsForIndexPath:(NSIndexPath *)path
{
    long index = path.row * self.columns;
    long length = MIN(self.columns, [self.elcAssets count] - index);
    return [self.elcAssets subarrayWithRange:NSMakeRange(index, length)];
}

// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"Cell";

    ELCAssetCell *cell = (ELCAssetCell*)[tableView dequeueReusableCellWithIdentifier:CellIdentifier];

    if (cell == nil) {
        cell = [[ELCAssetCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
        cell.width = self.cellWidth;
    }

    [cell setAssets:[self assetsForIndexPath:indexPath]];

    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return self.cellWidth;
}

- (int)totalSelectedAssets
{
    NSArray *selected = [[self selectedImages] allKeys];
    int count = (int) [selected count];

    for (ELCAsset *elcAsset in self.elcAssets) {
        NSString *identifier = [[elcAsset.asset getIdentifier] url];
        if (!elcAsset.selected && [selected containsObject:identifier]) {
            count--;
        }
        else if (elcAsset.selected && ![selected containsObject:identifier]) {
            count++;
        }
    }

    return count;
}


@end
