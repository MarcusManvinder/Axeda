#import <Foundation/Foundation.h>
#import "USAdditions.h"
#import <libxml/tree.h>
#import "USGlobals.h"
#import <objc/runtime.h>
/* Cookies handling provided by http://en.wikibooks.org/wiki/Programming:WebObjects/Web_Services/Web_Service_Provider */
#import <libxml/parser.h>
#import "ns1.h"
#import "GeneralServiceSvc.h"
#import "tns1.h"
@class GeneralSoapBinding;
@interface GeneralServiceSvc : NSObject {
	
}
+ (GeneralSoapBinding *)GeneralSoapBinding;
@end
@class GeneralSoapBindingResponse;
@class GeneralSoapBindingOperation;
@protocol GeneralSoapBindingResponseDelegate <NSObject>
- (void) operation:(GeneralSoapBindingOperation *)operation completedWithResponse:(GeneralSoapBindingResponse *)response;
@end
#define kServerAnchorCertificates   @"kServerAnchorCertificates"
#define kServerAnchorsOnly          @"kServerAnchorsOnly"
#define kClientIdentity             @"kClientIdentity"
#define kClientCertificates         @"kClientCertificates"
#define kClientUsername             @"kClientUsername"
#define kClientPassword             @"kClientPassword"
#define kNSURLCredentialPersistence @"kNSURLCredentialPersistence"
#define kValidateResult             @"kValidateResult"
@interface GeneralSoapBinding : NSObject <GeneralSoapBindingResponseDelegate> {
	NSURL *address;
	NSTimeInterval timeout;
	NSMutableArray *cookies;
	NSMutableDictionary *customHeaders;
	BOOL logXMLInOut;
	BOOL ignoreEmptyResponse;
	BOOL synchronousOperationComplete;
	id<SSLCredentialsManaging> sslManager;
	SOAPSigner *soapSigner;
}
@property (nonatomic, copy) NSURL *address;
@property (nonatomic) BOOL logXMLInOut;
@property (nonatomic) BOOL ignoreEmptyResponse;
@property (nonatomic) NSTimeInterval timeout;
@property (nonatomic, retain) NSMutableArray *cookies;
@property (nonatomic, retain) NSMutableDictionary *customHeaders;
@property (nonatomic, retain) id<SSLCredentialsManaging> sslManager;
@property (nonatomic, retain) SOAPSigner *soapSigner;
+ (NSTimeInterval) defaultTimeout;
- (id)initWithAddress:(NSString *)anAddress;
- (void)sendHTTPCallUsingBody:(NSString *)body soapAction:(NSString *)soapAction forOperation:(GeneralSoapBindingOperation *)operation;
- (void)addCookie:(NSHTTPCookie *)toAdd;
- (NSString *)MIMEType;
- (GeneralSoapBindingResponse *)getVersionUsingAuthentication:(tns1_SessionInfo *)aAuthentication ;
- (void)getVersionAsyncUsingAuthentication:(tns1_SessionInfo *)aAuthentication  delegate:(id<GeneralSoapBindingResponseDelegate>)responseDelegate;
- (GeneralSoapBindingResponse *)LoginUsingAuthentication:(tns1_SessionInfo *)aAuthentication ;
- (void)LoginAsyncUsingAuthentication:(tns1_SessionInfo *)aAuthentication  delegate:(id<GeneralSoapBindingResponseDelegate>)responseDelegate;
@end
@interface GeneralSoapBindingOperation : NSOperation {
	GeneralSoapBinding *binding;
	GeneralSoapBindingResponse *response;
	id<GeneralSoapBindingResponseDelegate> delegate;
	NSMutableData *responseData;
	NSURLConnection *urlConnection;
}
@property (nonatomic, retain) GeneralSoapBinding *binding;
@property (nonatomic, readonly) GeneralSoapBindingResponse *response;
@property (nonatomic, assign) id<GeneralSoapBindingResponseDelegate> delegate;
@property (nonatomic, retain) NSMutableData *responseData;
@property (nonatomic, retain) NSURLConnection *urlConnection;
- (id)initWithBinding:(GeneralSoapBinding *)aBinding delegate:(id<GeneralSoapBindingResponseDelegate>)aDelegate;
@end
@interface GeneralSoapBinding_getVersion : GeneralSoapBindingOperation {
	tns1_SessionInfo * Authentication;
}
@property (nonatomic, retain) tns1_SessionInfo * Authentication;
- (id)initWithBinding:(GeneralSoapBinding *)aBinding delegate:(id<GeneralSoapBindingResponseDelegate>)aDelegate
	Authentication:(tns1_SessionInfo *)aAuthentication
;
@end
@interface GeneralSoapBinding_Login : GeneralSoapBindingOperation {
	tns1_SessionInfo * Authentication;
}
@property (nonatomic, retain) tns1_SessionInfo * Authentication;
- (id)initWithBinding:(GeneralSoapBinding *)aBinding delegate:(id<GeneralSoapBindingResponseDelegate>)aDelegate
	Authentication:(tns1_SessionInfo *)aAuthentication
;
@end
@interface GeneralSoapBinding_envelope : NSObject {
}
+ (GeneralSoapBinding_envelope *)sharedInstance;
- (NSString *)serializedFormUsingHeaderElements:(NSDictionary *)headerElements bodyElements:(NSDictionary *)bodyElements bodyKeys:(NSArray *)bodyKeys;
@end
@interface GeneralSoapBindingResponse : NSObject {
	NSArray *headers;
	NSArray *bodyParts;
	NSError *error;
}
@property (nonatomic, retain) NSArray *headers;
@property (nonatomic, retain) NSArray *bodyParts;
@property (nonatomic, retain) NSError *error;
@end
