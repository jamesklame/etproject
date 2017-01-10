import com.kobil.test.system.script.ScriptClassRunner;
import com.kobil.test.system.script.JUnitBaseClass
import org.junit.*

class JUnitTest extends JUnitBaseClass{
	
	def asmUtils = include "/utils/asm/asmUtils.groovy"
	def sdkUtils = include "/utils/sdk/sdkUtils.groovy"
	def advSetUtils = include "/utils/ssms/advSetUtils.groovy"
	def AstStatus = include "/utils/sdk/enums/astStatus.groovy"
	def AstConfigParameters = include "/utils/sdk/enums/astConfigParameter.groovy"
	def AstConfirmation = include "/utils/sdk/enums/astConfirmation.groovy"
	def SdkApiAlerts = include "/utils/sdk/enums/sdkApiAlerts.groovy"
	
	def sdkApp
	
	@Before
	void setUp(){
		INPUT.userId("ktu_user")
		INPUT.pin("123456")
		
		advSetUtils.setAdvancedSetting("loginMaximumRetries", "5")
		advSetUtils.setAdvancedSetting("loginMaximumRetriesWithoutDelay", "5")
		advSetUtils.setAdvancedSetting("loginRetryFirstDelay", "60")
		
		sdkApp = sdkUtils.getSdkApp()
		sdkApp.info.configParams[AstConfigParameters.ALLOW_OFFLINE_PIN_VERIFICATION.key] = "true"
	}
	
	@Test
	void test(){
		sdkUtils.deactivateSdk(sdkApp)
		sdkUtils.setAppToState(sdkApp, "onLoginEnd")
		sdkUtils.setAppToState(sdkApp, "onLoginBegin")
		
		sdkApp.doSetUserId(INPUT.userId);
		sdkApp.waitFor("onSetUserIdEnd", [status:AstStatus.OK])
		
		sdkApp.doRegisterOfflineFunctions()
		
		sdkApp.waitFor("OfflineFunctions_onProvidePinBegin")
		sdkApp.OfflineFunctions_doProvidePin(AstConfirmation.TIMEOUT.key, INPUT.pin)
		sdkApp.waitFor("OfflineFunctions_onProvidePinEnd", [status:AstStatus.INVALID_PARAMETER])
		sdkApp.waitFor("onReport", [reportId:SdkApiAlerts.PROVIDE_PIN_PARAMETER_WRONG_CONFIRMATIONTYPE])
		
		sdkApp.OfflineFunctions_doProvidePin(AstConfirmation.CANCEL.key, "")
		sdkApp.waitFor("OfflineFunctions_onProvidePinEnd", [status:AstStatus.USER_CANCEL])
		
		sdkApp.waitFor("onRegisterOfflineFunctionsEnd", [status:AstStatus.USER_CANCEL])
	}
	
	@After
	void tearDown(){
	}
}

JUnitTest.scriptBaseClass = this
ScriptClassRunner.execute(JUnitTest)
