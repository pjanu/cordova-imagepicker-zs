//
//  AlbumPickerController.m
//
//  Created by ELC on 2/15/11.
//  Copyright 2011 ELC Technologies. All rights reserved.
//

#import "ELCAlbumPickerController.h"
#import "ELCImagePickerController.h"
#import "ELCAssetTablePicker.h"
#import "LocalizedString.h"
#import "AssetLibraryPhotoAlbum.h"
#import "AssetLibraryPhotoLibrary.h"

@implementation ELCAlbumPickerController

//Using auto synthesizers

#pragma mark -
#pragma mark View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];

    [self.navigationItem setTitle:[LocalizedString get:@"Loading..."]];

    UIBarButtonItem *cancelButton = [[UIBarButtonItem alloc] initWithTitle:[LocalizedString get:@"Cancel"] style:UIBarButtonItemStyleDone target:self.parent action:@selector(cancelImagePicker)];
    [self.navigationItem setRightBarButtonItem:cancelButton];

    UIBarButtonItem *backButton = [[UIBarButtonItem alloc] initWithTitle:[LocalizedString get:@"Back"] style:UIBarButtonItemStyleBordered target:nil action:nil];
    [self.navigationItem setBackBarButtonItem:backButton];

    self.assetGroups = [self.library fetchAlbums:^{
        [self performSelectorOnMainThread:@selector(reloadTableView) withObject:nil waitUntilDone:YES];
    }];
}

- (void)reloadTableView
{
    [self.tableView reloadData];
    [self.navigationItem setTitle:[LocalizedString get:@"Select an Album"]];
}

- (BOOL)shouldSelectAsset:(ELCAsset *)asset previousCount:(NSUInteger)previousCount
{
    return [self.parent shouldSelectAsset:asset previousCount:previousCount];
}

- (void)selectedAssets:(NSArray*)assets
{
    [_parent selectedAssets:assets];
}

#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    // Return the number of sections.
    return 1;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    // Return the number of rows in the section.
    return [self.assetGroups count];
}


// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"Cell";

    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
    }

    NSObject<PhotoAlbum> *album = (NSObject<PhotoAlbum>*)[self.assetGroups objectAtIndex:indexPath.row];

    cell.textLabel.text = [NSString stringWithFormat:@"%@ (%ld)",[album getTitle], (long) [album getCount]];
    [cell.imageView setImage:[album getThumbnail]];
    [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];

    return cell;
}

#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    ELCAssetTablePicker *picker = [[ELCAssetTablePicker alloc] initWithNibName: nil bundle: nil];
    picker.parent = self;
    picker.selection = [(id) self.parent getSelection];
    picker.titleStyle = [(id) self.parent titleStyle];
    picker.limitedOrientation = [(id) self.parent limitedOrientation];
    picker.selectedImages = self.selectedImages;
    picker.overlayColor = [(id) self.parent overlayColor];

    picker.album = [self.assetGroups objectAtIndex:indexPath.row];

    picker.assetPickerFilterDelegate = self.assetPickerFilterDelegate;
    picker.immediateReturn = self.immediateReturn;
    picker.singleSelection = self.singleSelection;

    picker.simpleHeader = [(id) self.parent simpleHeader];
    picker.countOkEval = [(id) self.parent countOkEval];

    [self.navigationController pushViewController:picker animated:YES];
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 57;
}

@end

