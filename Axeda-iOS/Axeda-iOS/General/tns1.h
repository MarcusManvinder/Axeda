#import <Foundation/Foundation.h>
#import "USAdditions.h"
#import <libxml/tree.h>
#import "USGlobals.h"
#import <objc/runtime.h>
@class tns1_SessionInfo;
@interface tns1_SessionInfo : NSObject <NSCoding> {
SOAPSigner *soapSigner;
/* elements */
	NSString * User;
	NSString * Password;
	NSString * sessionID;
/* attributes */
}
- (NSString *)nsPrefix;
- (xmlNodePtr)xmlNodeForDoc:(xmlDocPtr)doc elementName:(NSString *)elName elementNSPrefix:(NSString *)elNSPrefix;
- (void)addAttributesToNode:(xmlNodePtr)node;
- (void)addElementsToNode:(xmlNodePtr)node;
+ (tns1_SessionInfo *)deserializeNode:(xmlNodePtr)cur;
- (void)deserializeAttributesFromNode:(xmlNodePtr)cur;
- (void)deserializeElementsFromNode:(xmlNodePtr)cur;
@property (retain) SOAPSigner *soapSigner;
/* elements */
@property (nonatomic, retain) NSString * User;
@property (nonatomic, retain) NSString * Password;
@property (nonatomic, retain) NSString * sessionID;
/* attributes */
- (NSDictionary *)attributes;
@end
