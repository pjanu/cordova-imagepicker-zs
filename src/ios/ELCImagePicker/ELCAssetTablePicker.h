//
//  ELCAssetTablePicker.h
//
//  Created by ELC on 2/15/11.
//  Copyright 2011 ELC Technologies. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <AssetsLibrary/AssetsLibrary.h>
#import "ELCAsset.h"
#import "ELCAssetSelectionDelegate.h"
#import "ELCAssetPickerFilterDelegate.h"
#import "SelectionParameters.h"
#import "AssetPickerTitleStyle.h"
#import "InterfaceOrientation.h"
#import "Spinner.h"

@interface ELCAssetTablePicker : UITableViewController <ELCAssetDelegate>

@property (nonatomic, weak) id <ELCAssetSelectionDelegate> parent;
@property (nonatomic, strong) ALAssetsGroup *assetGroup;
@property (nonatomic, strong) NSMutableArray *elcAssets;
@property (nonatomic, strong) IBOutlet UILabel *selectedAssetsLabel;
@property (nonatomic, assign) BOOL singleSelection;
@property (nonatomic, assign) BOOL immediateReturn;
@property (nonatomic, strong) SelectionParameters *selection;
@property (nonatomic, strong) AssetPickerTitleStyle *titleStyle;
@property (nonatomic, strong) InterfaceOrientation *limitedOrientation;
@property (nonatomic, strong) Spinner *spinner;
@property (nonatomic, strong) NSMutableDictionary *selectedImages;

// optional, can be used to filter the assets displayed
@property(nonatomic, weak) id<ELCAssetPickerFilterDelegate> assetPickerFilterDelegate;

- (int)totalSelectedAssets;
- (void)preparePhotos;
- (void)setTitle:(NSString *)title;
- (void)updateSelectedCount;
- (void)doneAction:(id)sender;
- (void)backAction;

@end
