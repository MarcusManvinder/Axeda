#import "GeneralServiceSvc.h"
#import <libxml/xmlstring.h>
#if TARGET_OS_IPHONE
#import <CFNetwork/CFNetwork.h>
#endif
#ifndef ADVANCED_AUTHENTICATION
#define ADVANCED_AUTHENTICATION 0
#endif
#if ADVANCED_AUTHENTICATION && TARGET_OS_IPHONE
#import <Security/Security.h>
#endif
@implementation GeneralServiceSvc
+ (void)initialize
{
	[[USGlobals sharedInstance].wsdlStandardNamespaces setObject:@"ns1" forKey:@"http://www.w3.org/2001/XMLSchema"];
	[[USGlobals sharedInstance].wsdlStandardNamespaces setObject:@"GeneralServiceSvc" forKey:@"http://sdk.axeda.com/webservices/services/General"];
	[[USGlobals sharedInstance].wsdlStandardNamespaces setObject:@"tns1" forKey:@"http://sdk.axeda.com/webservices/type"];
}
+ (GeneralSoapBinding *)GeneralSoapBinding
{
	return [[[GeneralSoapBinding alloc] initWithAddress:@"http://sdk.axeda.com/webservices/services/General"] autorelease];
}
@end
@implementation GeneralSoapBinding
@synthesize address;
@synthesize timeout;
@synthesize logXMLInOut;
@synthesize ignoreEmptyResponse;
@synthesize cookies;
@synthesize customHeaders;
@synthesize soapSigner;
@synthesize sslManager;
+ (NSTimeInterval)defaultTimeout
{
	return 10;
}
- (id)init
{
	if((self = [super init])) {
		address = nil;
		cookies = nil;
		customHeaders = [NSMutableDictionary new];
		timeout = [[self class] defaultTimeout];
		logXMLInOut = NO;
		synchronousOperationComplete = NO;
	}
	
	return self;
}
- (id)initWithAddress:(NSString *)anAddress
{
	if((self = [self init])) {
		self.address = [NSURL URLWithString:anAddress];
	}
	
	return self;
}
- (NSString *)MIMEType
{
	return @"text/xml";
}
- (void)addCookie:(NSHTTPCookie *)toAdd
{
	if(toAdd != nil) {
		if(cookies == nil) cookies = [[NSMutableArray alloc] init];
		[cookies addObject:toAdd];
	}
}
- (GeneralSoapBindingResponse *)performSynchronousOperation:(GeneralSoapBindingOperation *)operation
{
	synchronousOperationComplete = NO;
	[operation start];
	
	// Now wait for response
	NSRunLoop *theRL = [NSRunLoop currentRunLoop];
	
	while (!synchronousOperationComplete && [theRL runMode:NSDefaultRunLoopMode beforeDate:[NSDate distantFuture]]);
	return operation.response;
}
- (void)performAsynchronousOperation:(GeneralSoapBindingOperation *)operation
{
	[operation start];
}
- (void) operation:(GeneralSoapBindingOperation *)operation completedWithResponse:(GeneralSoapBindingResponse *)response
{
	synchronousOperationComplete = YES;
}
- (GeneralSoapBindingResponse *)getVersionUsingAuthentication:(tns1_SessionInfo *)aAuthentication 
{
	return [self performSynchronousOperation:[[(GeneralSoapBinding_getVersion*)[GeneralSoapBinding_getVersion alloc] initWithBinding:self delegate:self
																							Authentication:aAuthentication
																							] autorelease]];
}
- (void)getVersionAsyncUsingAuthentication:(tns1_SessionInfo *)aAuthentication  delegate:(id<GeneralSoapBindingResponseDelegate>)responseDelegate
{
	[self performAsynchronousOperation: [[(GeneralSoapBinding_getVersion*)[GeneralSoapBinding_getVersion alloc] initWithBinding:self delegate:responseDelegate
																							 Authentication:aAuthentication
																							 ] autorelease]];
}
- (GeneralSoapBindingResponse *)LoginUsingAuthentication:(tns1_SessionInfo *)aAuthentication 
{
	return [self performSynchronousOperation:[[(GeneralSoapBinding_Login*)[GeneralSoapBinding_Login alloc] initWithBinding:self delegate:self
																							Authentication:aAuthentication
																							] autorelease]];
}
- (void)LoginAsyncUsingAuthentication:(tns1_SessionInfo *)aAuthentication  delegate:(id<GeneralSoapBindingResponseDelegate>)responseDelegate
{
	[self performAsynchronousOperation: [[(GeneralSoapBinding_Login*)[GeneralSoapBinding_Login alloc] initWithBinding:self delegate:responseDelegate
																							 Authentication:aAuthentication
																							 ] autorelease]];
}
- (void)sendHTTPCallUsingBody:(NSString *)outputBody soapAction:(NSString *)soapAction forOperation:(GeneralSoapBindingOperation *)operation
{
    if (!outputBody) {
        NSError * err = [NSError errorWithDomain:@"GeneralSoapBindingNULLRequestExcpetion"
                                            code:0
                                        userInfo:nil];
        
        [operation connection:nil didFailWithError:err];
        return;
    }
	NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:self.address 
																												 cachePolicy:NSURLRequestReloadIgnoringLocalAndRemoteCacheData
																										 timeoutInterval:self.timeout];
	NSData *bodyData = [outputBody dataUsingEncoding:NSUTF8StringEncoding];
	
	if(cookies != nil) {
		[request setAllHTTPHeaderFields:[NSHTTPCookie requestHeaderFieldsWithCookies:cookies]];
	}
	[request setValue:@"wsdl2objc" forHTTPHeaderField:@"User-Agent"];
	[request setValue:soapAction forHTTPHeaderField:@"SOAPAction"];
	[request setValue:[[self MIMEType] stringByAppendingString:@"; charset=utf-8"] forHTTPHeaderField:@"Content-Type"];
	[request setValue:[NSString stringWithFormat:@"%u", [bodyData length]] forHTTPHeaderField:@"Content-Length"];
	[request setValue:self.address.host forHTTPHeaderField:@"Host"];
	for (NSString *eachHeaderField in [self.customHeaders allKeys]) {
		[request setValue:[self.customHeaders objectForKey:eachHeaderField] forHTTPHeaderField:eachHeaderField];
	}
	[request setHTTPMethod: @"POST"];
	// set version 1.1 - how?
	[request setHTTPBody: bodyData];
		
	if(self.logXMLInOut) {
		NSLog(@"OutputHeaders:\n%@", [request allHTTPHeaderFields]);
		NSLog(@"OutputBody:\n%@", outputBody);
	}
	
	NSURLConnection *connection = [[NSURLConnection alloc] initWithRequest:request delegate:operation];
	
	operation.urlConnection = connection;
	[connection release];
}
- (void) dealloc
{
	[soapSigner release];
	[sslManager release];
	[address release];
	[cookies release];
	[customHeaders release];
	[super dealloc];
}
@end
@implementation GeneralSoapBindingOperation
@synthesize binding;
@synthesize response;
@synthesize delegate;
@synthesize responseData;
@synthesize urlConnection;
- (id)initWithBinding:(GeneralSoapBinding *)aBinding delegate:(id<GeneralSoapBindingResponseDelegate>)aDelegate
{
	if ((self = [super init])) {
		self.binding = aBinding;
		response = nil;
		self.delegate = aDelegate;
		self.responseData = nil;
		self.urlConnection = nil;
	}
	
	return self;
}
- (BOOL)connection:(NSURLConnection *)connection canAuthenticateAgainstProtectionSpace:(NSURLProtectionSpace *)protectionSpace {
    return [binding.sslManager canAuthenticateForAuthenticationMethod:protectionSpace.authenticationMethod];
}
-(void)connection:(NSURLConnection *)connection didReceiveAuthenticationChallenge:(NSURLAuthenticationChallenge *)challenge
{
	if (![binding.sslManager authenticateForChallenge:challenge]) {
		[[challenge sender] cancelAuthenticationChallenge:challenge];
	}
}
- (void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)urlResponse
{
	NSHTTPURLResponse *httpResponse;
	if ([urlResponse isKindOfClass:[NSHTTPURLResponse class]]) {
		httpResponse = (NSHTTPURLResponse *) urlResponse;
	} else {
		httpResponse = nil;
	}
	
	if(self.binding.logXMLInOut) {
		NSLog(@"ResponseStatus: %ld\n", (long)[httpResponse statusCode]);
		NSLog(@"ResponseHeaders:\n%@", [httpResponse allHeaderFields]);
	}
    NSInteger contentLength = [[[httpResponse allHeaderFields] objectForKey:@"Content-Length"] integerValue];
	
	if ([urlResponse.MIMEType rangeOfString:[self.binding MIMEType]].length == 0) {
		if ((self.binding.ignoreEmptyResponse == NO) || (contentLength != 0)) {
			NSError *error = nil;
			[connection cancel];
			if ([httpResponse statusCode] >= 400) {
				NSDictionary *userInfo = [NSDictionary dictionaryWithObjectsAndKeys:[NSHTTPURLResponse localizedStringForStatusCode:[httpResponse statusCode]],NSLocalizedDescriptionKey,
																			  httpResponse.URL, NSURLErrorKey,nil];
				error = [NSError errorWithDomain:@"GeneralSoapBindingResponseHTTP" code:[httpResponse statusCode] userInfo:userInfo];
			} else {
				NSDictionary *userInfo = [NSDictionary dictionaryWithObjectsAndKeys:
														[NSString stringWithFormat: @"Unexpected response MIME type to SOAP call:%@", urlResponse.MIMEType],NSLocalizedDescriptionKey,
																			  httpResponse.URL, NSURLErrorKey,nil];
				error = [NSError errorWithDomain:@"GeneralSoapBindingResponseHTTP" code:1 userInfo:userInfo];
			}
				
			[self connection:connection didFailWithError:error];
		} else {
            [delegate operation:self completedWithResponse:response];
		}
	}
}
- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data
{
	if (responseData == nil) {
		responseData = [data mutableCopy];
	} else {
		[responseData appendData:data];
	}
}
- (void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error
{
	if (binding.logXMLInOut) {
		NSLog(@"ResponseError:\n%@", error);
	}
	response.error = error;
	[delegate operation:self completedWithResponse:response];
}
- (void)dealloc
{
	[binding release];
	[response release];
	delegate = nil;
	[responseData release];
	[urlConnection release];
	
	[super dealloc];
}
@end
@implementation GeneralSoapBinding_getVersion
@synthesize Authentication;
- (id)initWithBinding:(GeneralSoapBinding *)aBinding delegate:(id<GeneralSoapBindingResponseDelegate>)responseDelegate
Authentication:(tns1_SessionInfo *)aAuthentication
{
	if((self = [super initWithBinding:aBinding delegate:responseDelegate])) {
		self.Authentication = aAuthentication;
	}
	
	return self;
}
- (void)dealloc
{
	if(Authentication != nil) [Authentication release];
	
	[super dealloc];
}
- (void)main
{
	[response autorelease];
	response = [GeneralSoapBindingResponse new];
	
	GeneralSoapBinding_envelope *envelope = [GeneralSoapBinding_envelope sharedInstance];
	
	NSMutableDictionary *headerElements = nil;
	headerElements = [NSMutableDictionary dictionary];
	if(Authentication != nil) [headerElements setObject:Authentication forKey:@"Authentication"];
	
	NSMutableDictionary *bodyElements = nil;
	NSMutableArray *bodyKeys = nil;
	bodyElements = [NSMutableDictionary dictionary];
	bodyKeys = [NSMutableArray array];
	id obj = nil;
	
	NSString *operationXMLString = [envelope serializedFormUsingHeaderElements:headerElements bodyElements:bodyElements bodyKeys:bodyKeys];
	operationXMLString = binding.soapSigner ? [binding.soapSigner signRequest:operationXMLString] : operationXMLString;
	
	[binding sendHTTPCallUsingBody:operationXMLString soapAction:@"getVersion" forOperation:self];
}
- (void)connectionDidFinishLoading:(NSURLConnection *)connection
{
	if (responseData != nil && delegate != nil)
	{
		xmlDocPtr doc;
		xmlNodePtr cur;
		
		if (binding.logXMLInOut) {
			NSLog(@"ResponseBody:\n%@", [[[NSString alloc] initWithData:responseData encoding:NSUTF8StringEncoding] autorelease]);
		}
		
#if !TARGET_OS_IPHONE && (!defined(MAC_OS_X_VERSION_10_6) || MAC_OS_X_VERSION_MIN_REQUIRED < MAC_OS_X_VERSION_10_6)
	// Not yet defined in 10.5 libxml
	#define XML_PARSE_COMPACT 0
#endif
	doc = xmlReadMemory([responseData bytes], [responseData length], NULL, NULL, XML_PARSE_COMPACT | XML_PARSE_NOBLANKS);
		
		if (doc == NULL) {
			NSDictionary *userInfo = [NSDictionary dictionaryWithObject:@"Errors while parsing returned XML" forKey:NSLocalizedDescriptionKey];
			
			response.error = [NSError errorWithDomain:@"GeneralSoapBindingResponseXML" code:1 userInfo:userInfo];
			[delegate operation:self completedWithResponse:response];
		} else {
			cur = xmlDocGetRootElement(doc);
			cur = cur->children;
			
			for( ; cur != NULL ; cur = cur->next) {
				if(cur->type == XML_ELEMENT_NODE) {
					
					if(xmlStrEqual(cur->name, (const xmlChar *) "Body")) {
						NSMutableArray *responseBodyParts = [NSMutableArray array];
						
						xmlNodePtr bodyNode;
						for(bodyNode=cur->children ; bodyNode != NULL ; bodyNode = bodyNode->next) {
							if(cur->type == XML_ELEMENT_NODE) {
								if(xmlStrEqual(bodyNode->name, (const xmlChar *) "version")) {
									NSString *bodyObject = [NSString deserializeNode:bodyNode];
									//NSAssert1(bodyObject != nil, @"Errors while parsing body %s", bodyNode->name);
									if (bodyObject != nil) [responseBodyParts addObject:bodyObject];
								}
								if ((bodyNode->ns != nil && xmlStrEqual(bodyNode->ns->prefix, cur->ns->prefix)) && 
									xmlStrEqual(bodyNode->name, (const xmlChar *) "Fault")) {
                                    NSDictionary *exceptions = [NSDictionary dictionaryWithObjectsAndKeys:
                                                                nil];
									SOAPFault *bodyObject = [SOAPFault deserializeNode:bodyNode expectedExceptions:exceptions];
									//NSAssert1(bodyObject != nil, @"Errors while parsing body %s", bodyNode->name);
									if (bodyObject != nil) [responseBodyParts addObject:bodyObject];
								}
							}
						}
						
						response.bodyParts = responseBodyParts;
					}
				}
			}
			
			xmlFreeDoc(doc);
		}
		
		xmlCleanupParser();
		[delegate operation:self completedWithResponse:response];
	}
}
@end
@implementation GeneralSoapBinding_Login
@synthesize Authentication;
- (id)initWithBinding:(GeneralSoapBinding *)aBinding delegate:(id<GeneralSoapBindingResponseDelegate>)responseDelegate
Authentication:(tns1_SessionInfo *)aAuthentication
{
	if((self = [super initWithBinding:aBinding delegate:responseDelegate])) {
		self.Authentication = aAuthentication;
	}
	
	return self;
}
- (void)dealloc
{
	if(Authentication != nil) [Authentication release];
	
	[super dealloc];
}
- (void)main
{
	[response autorelease];
	response = [GeneralSoapBindingResponse new];
	
	GeneralSoapBinding_envelope *envelope = [GeneralSoapBinding_envelope sharedInstance];
	
	NSMutableDictionary *headerElements = nil;
	headerElements = [NSMutableDictionary dictionary];
	if(Authentication != nil) [headerElements setObject:Authentication forKey:@"Authentication"];
	
	NSMutableDictionary *bodyElements = nil;
	NSMutableArray *bodyKeys = nil;
	bodyElements = [NSMutableDictionary dictionary];
	bodyKeys = [NSMutableArray array];
	id obj = nil;
	
	NSString *operationXMLString = [envelope serializedFormUsingHeaderElements:headerElements bodyElements:bodyElements bodyKeys:bodyKeys];
	operationXMLString = binding.soapSigner ? [binding.soapSigner signRequest:operationXMLString] : operationXMLString;
	
	[binding sendHTTPCallUsingBody:operationXMLString soapAction:@"Login" forOperation:self];
}
- (void)connectionDidFinishLoading:(NSURLConnection *)connection
{
	if (responseData != nil && delegate != nil)
	{
		xmlDocPtr doc;
		xmlNodePtr cur;
		
		if (binding.logXMLInOut) {
			NSLog(@"ResponseBody:\n%@", [[[NSString alloc] initWithData:responseData encoding:NSUTF8StringEncoding] autorelease]);
		}
		
#if !TARGET_OS_IPHONE && (!defined(MAC_OS_X_VERSION_10_6) || MAC_OS_X_VERSION_MIN_REQUIRED < MAC_OS_X_VERSION_10_6)
	// Not yet defined in 10.5 libxml
	#define XML_PARSE_COMPACT 0
#endif
	doc = xmlReadMemory([responseData bytes], [responseData length], NULL, NULL, XML_PARSE_COMPACT | XML_PARSE_NOBLANKS);
		
		if (doc == NULL) {
			NSDictionary *userInfo = [NSDictionary dictionaryWithObject:@"Errors while parsing returned XML" forKey:NSLocalizedDescriptionKey];
			
			response.error = [NSError errorWithDomain:@"GeneralSoapBindingResponseXML" code:1 userInfo:userInfo];
			[delegate operation:self completedWithResponse:response];
		} else {
			cur = xmlDocGetRootElement(doc);
			cur = cur->children;
			
			for( ; cur != NULL ; cur = cur->next) {
				if(cur->type == XML_ELEMENT_NODE) {
					
					if(xmlStrEqual(cur->name, (const xmlChar *) "Body")) {
						NSMutableArray *responseBodyParts = [NSMutableArray array];
						
						xmlNodePtr bodyNode;
						for(bodyNode=cur->children ; bodyNode != NULL ; bodyNode = bodyNode->next) {
							if(cur->type == XML_ELEMENT_NODE) {
								if(xmlStrEqual(bodyNode->name, (const xmlChar *) "sessionID")) {
									NSString *bodyObject = [NSString deserializeNode:bodyNode];
									//NSAssert1(bodyObject != nil, @"Errors while parsing body %s", bodyNode->name);
									if (bodyObject != nil) [responseBodyParts addObject:bodyObject];
								}
								if ((bodyNode->ns != nil && xmlStrEqual(bodyNode->ns->prefix, cur->ns->prefix)) && 
									xmlStrEqual(bodyNode->name, (const xmlChar *) "Fault")) {
                                    NSDictionary *exceptions = [NSDictionary dictionaryWithObjectsAndKeys:
                                                                nil];
									SOAPFault *bodyObject = [SOAPFault deserializeNode:bodyNode expectedExceptions:exceptions];
									//NSAssert1(bodyObject != nil, @"Errors while parsing body %s", bodyNode->name);
									if (bodyObject != nil) [responseBodyParts addObject:bodyObject];
								}
							}
						}
						
						response.bodyParts = responseBodyParts;
					}
				}
			}
			
			xmlFreeDoc(doc);
		}
		
		xmlCleanupParser();
		[delegate operation:self completedWithResponse:response];
	}
}
@end
static GeneralSoapBinding_envelope *GeneralSoapBindingSharedEnvelopeInstance = nil;
@implementation GeneralSoapBinding_envelope
+ (GeneralSoapBinding_envelope *)sharedInstance
{
	if(GeneralSoapBindingSharedEnvelopeInstance == nil) {
		GeneralSoapBindingSharedEnvelopeInstance = [GeneralSoapBinding_envelope new];
	}
	
	return GeneralSoapBindingSharedEnvelopeInstance;
}
- (NSString *)serializedFormUsingHeaderElements:(NSDictionary *)headerElements bodyElements:(NSDictionary *)bodyElements bodyKeys:(NSArray *)bodyKeys
{
	xmlDocPtr doc;
	
	doc = xmlNewDoc((const xmlChar*)XML_DEFAULT_VERSION);
	if (doc == NULL) {
		NSLog(@"Error creating the xml document tree");
		return @"";
	}
	
	xmlNodePtr root = xmlNewDocNode(doc, NULL, (const xmlChar*)"Envelope", NULL);
	xmlDocSetRootElement(doc, root);
	
	xmlNsPtr soapEnvelopeNs = xmlNewNs(root, (const xmlChar*)"http://schemas.xmlsoap.org/soap/envelope/", (const xmlChar*)"soap");
	xmlSetNs(root, soapEnvelopeNs);
	
	xmlNsPtr xslNs = xmlNewNs(root, (const xmlChar*)"http://www.w3.org/1999/XSL/Transform", (const xmlChar*)"xsl");
	xmlNewNs(root, (const xmlChar*)"http://www.w3.org/2001/XMLSchema-instance", (const xmlChar*)"xsi");
	
	xmlNewNsProp(root, xslNs, (const xmlChar*)"version", (const xmlChar*)"1.0");
	
	xmlNewNs(root, (const xmlChar*)"http://www.w3.org/2001/XMLSchema", (const xmlChar*)"ns1");
	xmlNewNs(root, (const xmlChar*)"http://sdk.axeda.com/webservices/services/General", (const xmlChar*)"GeneralServiceSvc");
	xmlNewNs(root, (const xmlChar*)"http://sdk.axeda.com/webservices/type", (const xmlChar*)"tns1");
	
	if((headerElements != nil) && ([headerElements count] > 0)) {
		xmlNodePtr headerNode = xmlNewDocNode(doc, soapEnvelopeNs, (const xmlChar*)"Header", NULL);
		xmlAddChild(root, headerNode);
		
		for(NSString *key in [headerElements allKeys]) {
			id header = [headerElements objectForKey:key];
			xmlAddChild(headerNode, [header xmlNodeForDoc:doc elementName:key elementNSPrefix:nil]);
		}
	}
	
	if((bodyElements != nil) && ([bodyElements count] > 0)) {
		xmlNodePtr bodyNode = xmlNewDocNode(doc, soapEnvelopeNs, (const xmlChar*)"Body", NULL);
		xmlAddChild(root, bodyNode);
		
		for(NSString *key in bodyKeys) {
			id body = [bodyElements objectForKey:key];
			xmlAddChild(bodyNode, [body xmlNodeForDoc:doc elementName:key elementNSPrefix:nil]);
		}
	}
	
	xmlChar *buf;
	int size;
	xmlDocDumpFormatMemory(doc, &buf, &size, 1);
	
	NSString *serializedForm = [NSString stringWithCString:(const char*)buf encoding:NSUTF8StringEncoding];
	xmlFree(buf);
	
	xmlFreeDoc(doc);	
	return serializedForm;
}
@end
@implementation GeneralSoapBindingResponse
@synthesize headers;
@synthesize bodyParts;
@synthesize error;
- (id)init
{
	if((self = [super init])) {
		headers = nil;
		bodyParts = nil;
		error = nil;
	}
	
	return self;
}
- (void)dealloc {
	self.headers = nil;
	self.bodyParts = nil;
	self.error = nil;	
	[super dealloc];
}
@end
