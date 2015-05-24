set define off
set verify off
set feedback off
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
begin wwv_flow.g_import_in_progress := true; end;
/
 
--       AAAA       PPPPP   EEEEEE  XX      XX
--      AA  AA      PP  PP  EE       XX    XX
--     AA    AA     PP  PP  EE        XX  XX
--    AAAAAAAAAA    PPPPP   EEEE       XXXX
--   AA        AA   PP      EE        XX  XX
--  AA          AA  PP      EE       XX    XX
--  AA          AA  PP      EEEEEE  XX      XX
prompt  Set Credentials...
 
begin
 
  -- Assumes you are running the script connected to SQL*Plus as the Oracle user APEX_040200 or as the owner (parsing schema) of the application.
  wwv_flow_api.set_security_group_id(p_security_group_id=>nvl(wwv_flow_application_install.get_workspace_id,2810128596271087));
 
end;
/

begin wwv_flow.g_import_in_progress := true; end;
/
begin 

select value into wwv_flow_api.g_nls_numeric_chars from nls_session_parameters where parameter='NLS_NUMERIC_CHARACTERS';

end;

/
begin execute immediate 'alter session set nls_numeric_characters=''.,''';

end;

/
begin wwv_flow.g_browser_language := 'en'; end;
/
prompt  Check Compatibility...
 
begin
 
-- This date identifies the minimum version required to import this file.
wwv_flow_api.set_version(p_version_yyyy_mm_dd=>'2012.01.01');
 
end;
/

prompt  Set Application Offset...
 
begin
 
   -- SET APPLICATION OFFSET
   wwv_flow_api.g_id_offset := nvl(wwv_flow_application_install.get_offset,0);
null;
 
end;
/

 
begin
 
wwv_flow_api.remove_restful_service (
  p_id => 5622617744333346 + wwv_flow_api.g_id_offset
 ,p_name => 'parus.ua.udp'
  );
 
null;
 
end;
/

prompt  ...restful service
--
--application/restful_services/parus_ua_udp
 
begin
 
wwv_flow_api.create_restful_module (
  p_id => 5622617744333346 + wwv_flow_api.g_id_offset
 ,p_name => 'parus.ua.udp'
 ,p_uri_prefix => 'udp/'
 ,p_parsing_schema => 'REST'
 ,p_items_per_page => 50
 ,p_status => 'PUBLISHED'
  );
 
wwv_flow_api.create_restful_template (
  p_id => 2836332589133924 + wwv_flow_api.g_id_offset
 ,p_module_id => 5622617744333346 + wwv_flow_api.g_id_offset
 ,p_uri_template => 'claim/'
 ,p_priority => 0
 ,p_etag_type => 'HASH'
  );
 
wwv_flow_api.create_restful_handler (
  p_id => 2836429950142567 + wwv_flow_api.g_id_offset
 ,p_template_id => 2836332589133924 + wwv_flow_api.g_id_offset
 ,p_source_type => 'QUERY'
 ,p_format => 'DEFAULT'
 ,p_method => 'GET'
 ,p_require_https => 'NO'
 ,p_source => 
'select * from table(UDO_PKG_MOBILE_IFACE.CLAIM_BY_RN(:session,:rn))'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2836622603235133 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2836429950142567 + wwv_flow_api.g_id_offset
 ,p_name => 'rn'
 ,p_bind_variable_name => 'rn'
 ,p_source_type => 'HEADER'
 ,p_access_method => 'IN'
 ,p_param_type => 'LONG'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2836515330232977 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2836429950142567 + wwv_flow_api.g_id_offset
 ,p_name => 'session'
 ,p_bind_variable_name => 'session'
 ,p_source_type => 'HEADER'
 ,p_access_method => 'IN'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_template (
  p_id => 2845301280620316 + wwv_flow_api.g_id_offset
 ,p_module_id => 5622617744333346 + wwv_flow_api.g_id_offset
 ,p_uri_template => 'claim/add/'
 ,p_priority => 0
 ,p_etag_type => 'HASH'
  );
 
wwv_flow_api.create_restful_handler (
  p_id => 2845403836630578 + wwv_flow_api.g_id_offset
 ,p_template_id => 2845301280620316 + wwv_flow_api.g_id_offset
 ,p_source_type => 'PLSQL'
 ,p_format => 'DEFAULT'
 ,p_method => 'POST'
 ,p_require_https => 'YES'
 ,p_source => 
'begin udo_pkg_mobile_iface.claim_insert(:session,:ptype,:priority,:app,:unit,:func,:description,:relfound,:bldfound,:relfix,:rn,:error); end;'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2845604313640152 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2845403836630578 + wwv_flow_api.g_id_offset
 ,p_name => 'error'
 ,p_bind_variable_name => 'error'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2845532233638794 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2845403836630578 + wwv_flow_api.g_id_offset
 ,p_name => 'rn'
 ,p_bind_variable_name => 'rn'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'LONG'
  );
 
wwv_flow_api.create_restful_template (
  p_id => 2850113359821786 + wwv_flow_api.g_id_offset
 ,p_module_id => 5622617744333346 + wwv_flow_api.g_id_offset
 ,p_uri_template => 'claim/addnote/'
 ,p_priority => 0
 ,p_etag_type => 'HASH'
  );
 
wwv_flow_api.create_restful_handler (
  p_id => 2850217861823037 + wwv_flow_api.g_id_offset
 ,p_template_id => 2850113359821786 + wwv_flow_api.g_id_offset
 ,p_source_type => 'PLSQL'
 ,p_format => 'DEFAULT'
 ,p_method => 'POST'
 ,p_require_https => 'NO'
 ,p_source => 
'begin udo_pkg_mobile_iface.claim_add_note(:session,:rn,:note,:error,:result); end;'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2850308989830011 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2850217861823037 + wwv_flow_api.g_id_offset
 ,p_name => 'error'
 ,p_bind_variable_name => 'error'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2850416954832245 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2850217861823037 + wwv_flow_api.g_id_offset
 ,p_name => 'result'
 ,p_bind_variable_name => 'result'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'INT'
  );
 
wwv_flow_api.create_restful_template (
  p_id => 2849304038995934 + wwv_flow_api.g_id_offset
 ,p_module_id => 5622617744333346 + wwv_flow_api.g_id_offset
 ,p_uri_template => 'claim/close/'
 ,p_priority => 0
 ,p_etag_type => 'HASH'
  );
 
wwv_flow_api.create_restful_handler (
  p_id => 2849412696998450 + wwv_flow_api.g_id_offset
 ,p_template_id => 2849304038995934 + wwv_flow_api.g_id_offset
 ,p_source_type => 'PLSQL'
 ,p_format => 'DEFAULT'
 ,p_method => 'POST'
 ,p_require_https => 'NO'
 ,p_source => 
'begin udo_pkg_mobile_iface.claim_close(:session,:rn,:error,:result); end;'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2849522393001251 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2849412696998450 + wwv_flow_api.g_id_offset
 ,p_name => 'error'
 ,p_bind_variable_name => 'error'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2850009719782835 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2849412696998450 + wwv_flow_api.g_id_offset
 ,p_name => 'result'
 ,p_bind_variable_name => 'result'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'INT'
  );
 
wwv_flow_api.create_restful_template (
  p_id => 2849018293754107 + wwv_flow_api.g_id_offset
 ,p_module_id => 5622617744333346 + wwv_flow_api.g_id_offset
 ,p_uri_template => 'claim/delete/'
 ,p_priority => 0
 ,p_etag_type => 'HASH'
  );
 
wwv_flow_api.create_restful_handler (
  p_id => 2849120849764292 + wwv_flow_api.g_id_offset
 ,p_template_id => 2849018293754107 + wwv_flow_api.g_id_offset
 ,p_source_type => 'PLSQL'
 ,p_format => 'DEFAULT'
 ,p_method => 'POST'
 ,p_require_https => 'NO'
 ,p_source => 
'begin udo_pkg_mobile_iface.claim_delete(:session,:rn,:error,:result); end;'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2849227428766198 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2849120849764292 + wwv_flow_api.g_id_offset
 ,p_name => 'error'
 ,p_bind_variable_name => 'error'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2849826210778138 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2849120849764292 + wwv_flow_api.g_id_offset
 ,p_name => 'result'
 ,p_bind_variable_name => 'result'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'INT'
  );
 
wwv_flow_api.create_restful_template (
  p_id => 2845721282645043 + wwv_flow_api.g_id_offset
 ,p_module_id => 5622617744333346 + wwv_flow_api.g_id_offset
 ,p_uri_template => 'claim/edit/'
 ,p_priority => 0
 ,p_etag_type => 'HASH'
  );
 
wwv_flow_api.create_restful_handler (
  p_id => 2845827862646994 + wwv_flow_api.g_id_offset
 ,p_template_id => 2845721282645043 + wwv_flow_api.g_id_offset
 ,p_source_type => 'PLSQL'
 ,p_format => 'DEFAULT'
 ,p_method => 'POST'
 ,p_require_https => 'NO'
 ,p_source => 
'begin udo_pkg_mobile_iface.claim_update(:session,:rn,:description,:relfound,:bldfound,:relfix,:bldfix,:app,:unit,:func,:priority,:error); end;'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2845904790649717 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2845827862646994 + wwv_flow_api.g_id_offset
 ,p_name => 'error'
 ,p_bind_variable_name => 'error'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_template (
  p_id => 2838400101216044 + wwv_flow_api.g_id_offset
 ,p_module_id => 5622617744333346 + wwv_flow_api.g_id_offset
 ,p_uri_template => 'claimdocs/'
 ,p_priority => 0
 ,p_etag_type => 'HASH'
  );
 
wwv_flow_api.create_restful_handler (
  p_id => 2838514645220176 + wwv_flow_api.g_id_offset
 ,p_template_id => 2838400101216044 + wwv_flow_api.g_id_offset
 ,p_source_type => 'QUERY'
 ,p_format => 'DEFAULT'
 ,p_method => 'GET'
 ,p_require_https => 'NO'
 ,p_source => 
'select * from table(UDO_PKG_MOBILE_IFACE.CLAIM_DOCUMS(:session,:prn))'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2838631268224987 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2838514645220176 + wwv_flow_api.g_id_offset
 ,p_name => 'prn'
 ,p_bind_variable_name => 'prn'
 ,p_source_type => 'HEADER'
 ,p_access_method => 'IN'
 ,p_param_type => 'LONG'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2838704041226642 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2838514645220176 + wwv_flow_api.g_id_offset
 ,p_name => 'session'
 ,p_bind_variable_name => 'session'
 ,p_source_type => 'HEADER'
 ,p_access_method => 'IN'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_template (
  p_id => 2837519069622212 + wwv_flow_api.g_id_offset
 ,p_module_id => 5622617744333346 + wwv_flow_api.g_id_offset
 ,p_uri_template => 'claimhist/'
 ,p_priority => 0
 ,p_etag_type => 'HASH'
  );
 
wwv_flow_api.create_restful_handler (
  p_id => 2837606387628041 + wwv_flow_api.g_id_offset
 ,p_template_id => 2837519069622212 + wwv_flow_api.g_id_offset
 ,p_source_type => 'QUERY'
 ,p_format => 'DEFAULT'
 ,p_method => 'GET'
 ,p_require_https => 'NO'
 ,p_source => 
'select * from table(UDO_PKG_MOBILE_IFACE.CLAIM_HISTORY(:session,:prn))'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2837915182816153 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2837606387628041 + wwv_flow_api.g_id_offset
 ,p_name => 'prn'
 ,p_bind_variable_name => 'prn'
 ,p_source_type => 'HEADER'
 ,p_access_method => 'IN'
 ,p_param_type => 'LONG'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2837809295814453 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2837606387628041 + wwv_flow_api.g_id_offset
 ,p_name => 'session'
 ,p_bind_variable_name => 'session'
 ,p_source_type => 'HEADER'
 ,p_access_method => 'IN'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_template (
  p_id => 2827331904169398 + wwv_flow_api.g_id_offset
 ,p_module_id => 5622617744333346 + wwv_flow_api.g_id_offset
 ,p_uri_template => 'claims/'
 ,p_priority => 0
 ,p_etag_type => 'HASH'
  );
 
wwv_flow_api.create_restful_handler (
  p_id => 2827425587186477 + wwv_flow_api.g_id_offset
 ,p_template_id => 2827331904169398 + wwv_flow_api.g_id_offset
 ,p_source_type => 'QUERY'
 ,p_format => 'DEFAULT'
 ,p_method => 'GET'
 ,p_items_per_page => 25
 ,p_require_https => 'NO'
 ,p_source => 
'select * from table(UDO_PKG_MOBILE_IFACE.CLAIM_BY_COND_RN(:session,:cond,:newrn))'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2845124156428227 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2827425587186477 + wwv_flow_api.g_id_offset
 ,p_name => 'cond'
 ,p_bind_variable_name => 'cond'
 ,p_source_type => 'HEADER'
 ,p_access_method => 'IN'
 ,p_param_type => 'LONG'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2845215629435322 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2827425587186477 + wwv_flow_api.g_id_offset
 ,p_name => 'newrn'
 ,p_bind_variable_name => 'newrn'
 ,p_source_type => 'HEADER'
 ,p_access_method => 'IN'
 ,p_param_type => 'LONG'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2845012727425016 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2827425587186477 + wwv_flow_api.g_id_offset
 ,p_name => 'session'
 ,p_bind_variable_name => 'session'
 ,p_source_type => 'HEADER'
 ,p_access_method => 'IN'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_template (
  p_id => 2820813023342079 + wwv_flow_api.g_id_offset
 ,p_module_id => 5622617744333346 + wwv_flow_api.g_id_offset
 ,p_uri_template => 'dicts/applist/'
 ,p_priority => 0
 ,p_etag_type => 'HASH'
  );
 
wwv_flow_api.create_restful_handler (
  p_id => 2820908998350342 + wwv_flow_api.g_id_offset
 ,p_template_id => 2820813023342079 + wwv_flow_api.g_id_offset
 ,p_source_type => 'QUERY'
 ,p_format => 'DEFAULT'
 ,p_method => 'GET'
 ,p_require_https => 'NO'
 ,p_source => 
'select S01 as n from table(UDO_PKG_MOBILE_IFACE.APPLISTS)'
  );
 
wwv_flow_api.create_restful_template (
  p_id => 2822230122918113 + wwv_flow_api.g_id_offset
 ,p_module_id => 5622617744333346 + wwv_flow_api.g_id_offset
 ,p_uri_template => 'dicts/builds/{v}'
 ,p_priority => 0
 ,p_etag_type => 'HASH'
  );
 
wwv_flow_api.create_restful_handler (
  p_id => 2822307575958947 + wwv_flow_api.g_id_offset
 ,p_template_id => 2822230122918113 + wwv_flow_api.g_id_offset
 ,p_source_type => 'QUERY'
 ,p_format => 'DEFAULT'
 ,p_method => 'GET'
 ,p_require_https => 'NO'
 ,p_source => 
'select N01 as r, N02 as p, S01 as c, S02 as d from table(UDO_PKG_MOBILE_IFACE.BUILDS (:v))'
  );
 
wwv_flow_api.create_restful_template (
  p_id => 2821321771501744 + wwv_flow_api.g_id_offset
 ,p_module_id => 5622617744333346 + wwv_flow_api.g_id_offset
 ,p_uri_template => 'dicts/releases/'
 ,p_priority => 0
 ,p_etag_type => 'HASH'
  );
 
wwv_flow_api.create_restful_handler (
  p_id => 2821424327511924 + wwv_flow_api.g_id_offset
 ,p_template_id => 2821321771501744 + wwv_flow_api.g_id_offset
 ,p_source_type => 'QUERY'
 ,p_format => 'DEFAULT'
 ,p_method => 'GET'
 ,p_require_https => 'NO'
 ,p_source => 
'select N01 as rn, trim(S01) as v, S02 as r from table(UDO_PKG_MOBILE_IFACE.RELEASES)'
  );
 
wwv_flow_api.create_restful_template (
  p_id => 2824415971103797 + wwv_flow_api.g_id_offset
 ,p_module_id => 5622617744333346 + wwv_flow_api.g_id_offset
 ,p_uri_template => 'dicts/units/'
 ,p_priority => 0
 ,p_etag_type => 'HASH'
  );
 
wwv_flow_api.create_restful_handler (
  p_id => 2824506883120167 + wwv_flow_api.g_id_offset
 ,p_template_id => 2824415971103797 + wwv_flow_api.g_id_offset
 ,p_source_type => 'QUERY'
 ,p_format => 'DEFAULT'
 ,p_method => 'GET'
 ,p_items_per_page => 50
 ,p_require_https => 'NO'
 ,p_source => 
'select s01 as n from table(UDO_PKG_MOBILE_IFACE.UNITS)'
  );
 
wwv_flow_api.create_restful_template (
  p_id => 2840605033355968 + wwv_flow_api.g_id_offset
 ,p_module_id => 5622617744333346 + wwv_flow_api.g_id_offset
 ,p_uri_template => 'dicts/units/deps/'
 ,p_priority => 0
 ,p_etag_type => 'HASH'
  );
 
wwv_flow_api.create_restful_handler (
  p_id => 2840721581455317 + wwv_flow_api.g_id_offset
 ,p_template_id => 2840605033355968 + wwv_flow_api.g_id_offset
 ,p_source_type => 'QUERY'
 ,p_format => 'DEFAULT'
 ,p_method => 'GET'
 ,p_require_https => 'NO'
 ,p_source => 
'select * from table(UDO_PKG_MOBILE_IFACE.UNIT_DEPS(:unitname))'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2840831624458268 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2840721581455317 + wwv_flow_api.g_id_offset
 ,p_name => 'unitname'
 ,p_bind_variable_name => 'unitname'
 ,p_source_type => 'HEADER'
 ,p_access_method => 'IN'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_template (
  p_id => 2838928161666928 + wwv_flow_api.g_id_offset
 ,p_module_id => 5622617744333346 + wwv_flow_api.g_id_offset
 ,p_uri_template => 'docum/'
 ,p_priority => 0
 ,p_etag_type => 'HASH'
  );
 
wwv_flow_api.create_restful_handler (
  p_id => 2839104209256064 + wwv_flow_api.g_id_offset
 ,p_template_id => 2838928161666928 + wwv_flow_api.g_id_offset
 ,p_source_type => 'MEDIA'
 ,p_format => 'DEFAULT'
 ,p_method => 'GET'
 ,p_require_https => 'NO'
 ,p_source => 
'select mt as content_type, ct as content_body from table(UDO_PKG_MOBILE_IFACE.CLAIM_DOCUM(:session,:docrn))'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2839316676259693 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2839104209256064 + wwv_flow_api.g_id_offset
 ,p_name => 'docrn'
 ,p_bind_variable_name => 'docrn'
 ,p_source_type => 'HEADER'
 ,p_access_method => 'IN'
 ,p_param_type => 'LONG'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2839209750257725 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2839104209256064 + wwv_flow_api.g_id_offset
 ,p_name => 'session'
 ,p_bind_variable_name => 'session'
 ,p_source_type => 'HEADER'
 ,p_access_method => 'IN'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_handler (
  p_id => 2850802538887733 + wwv_flow_api.g_id_offset
 ,p_template_id => 2838928161666928 + wwv_flow_api.g_id_offset
 ,p_source_type => 'PLSQL'
 ,p_format => 'DEFAULT'
 ,p_method => 'POST'
 ,p_require_https => 'NO'
 ,p_source => 
'declare ct varchar2(1000); cb blob; fn varchar2(1000); begin ct := :contentType; cb := :body; fn := :filename; udo_pkg_mobile_iface.claim_add_doc(:session,:rn, fn, cb,:error,:result); end;'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2851718846036184 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2850802538887733 + wwv_flow_api.g_id_offset
 ,p_name => 'Parus-filename'
 ,p_bind_variable_name => 'filename'
 ,p_source_type => 'HEADER'
 ,p_access_method => 'IN'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2851613128996716 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2850802538887733 + wwv_flow_api.g_id_offset
 ,p_name => 'Parus-rn'
 ,p_bind_variable_name => 'rn'
 ,p_source_type => 'HEADER'
 ,p_access_method => 'IN'
 ,p_param_type => 'LONG'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2851508279995285 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2850802538887733 + wwv_flow_api.g_id_offset
 ,p_name => 'Parus-session'
 ,p_bind_variable_name => 'session'
 ,p_source_type => 'HEADER'
 ,p_access_method => 'IN'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2851018786365509 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2850802538887733 + wwv_flow_api.g_id_offset
 ,p_name => 'error'
 ,p_bind_variable_name => 'error'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2851123981367048 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2850802538887733 + wwv_flow_api.g_id_offset
 ,p_name => 'result'
 ,p_bind_variable_name => 'result'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'INT'
  );
 
wwv_flow_api.create_restful_template (
  p_id => 2831001783215378 + wwv_flow_api.g_id_offset
 ,p_module_id => 5622617744333346 + wwv_flow_api.g_id_offset
 ,p_uri_template => 'filter/'
 ,p_priority => 0
 ,p_etag_type => 'HASH'
  );
 
wwv_flow_api.create_restful_handler (
  p_id => 2831104685225737 + wwv_flow_api.g_id_offset
 ,p_template_id => 2831001783215378 + wwv_flow_api.g_id_offset
 ,p_source_type => 'PLSQL'
 ,p_format => 'DEFAULT'
 ,p_method => 'GET'
 ,p_require_https => 'NO'
 ,p_source => 
'begin udo_pkg_mobile_iface.get_filter(:session,:filterrn,:filtername,:claimnumb,:claimvers,:claimrelease,:claimbuild,:claimunit,:claimapp,:claimiminit,:claimimperf,:claimcontent,:error);end;'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2831220268230156 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2831104685225737 + wwv_flow_api.g_id_offset
 ,p_name => 'claimapp'
 ,p_bind_variable_name => 'claimapp'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2831331350233447 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2831104685225737 + wwv_flow_api.g_id_offset
 ,p_name => 'claimbuild'
 ,p_bind_variable_name => 'claimbuild'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2831407933236060 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2831104685225737 + wwv_flow_api.g_id_offset
 ,p_name => 'claimcontent'
 ,p_bind_variable_name => 'claimcontent'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2831519361239361 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2831104685225737 + wwv_flow_api.g_id_offset
 ,p_name => 'claimiminit'
 ,p_bind_variable_name => 'claimiminit'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'INT'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2831629750242359 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2831104685225737 + wwv_flow_api.g_id_offset
 ,p_name => 'claimimperf'
 ,p_bind_variable_name => 'claimimperf'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'INT'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2831715336247726 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2831104685225737 + wwv_flow_api.g_id_offset
 ,p_name => 'claimnumb'
 ,p_bind_variable_name => 'claimnumb'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2831823647250136 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2831104685225737 + wwv_flow_api.g_id_offset
 ,p_name => 'claimrelease'
 ,p_bind_variable_name => 'claimrelease'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2831929188251688 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2831104685225737 + wwv_flow_api.g_id_offset
 ,p_name => 'claimunit'
 ,p_bind_variable_name => 'claimunit'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2832002654253506 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2831104685225737 + wwv_flow_api.g_id_offset
 ,p_name => 'claimvers'
 ,p_bind_variable_name => 'claimvers'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2832109233255429 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2831104685225737 + wwv_flow_api.g_id_offset
 ,p_name => 'error'
 ,p_bind_variable_name => 'error'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2832216506257477 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2831104685225737 + wwv_flow_api.g_id_offset
 ,p_name => 'filtername'
 ,p_bind_variable_name => 'filtername'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2832322393259155 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2831104685225737 + wwv_flow_api.g_id_offset
 ,p_name => 'filterrn'
 ,p_bind_variable_name => 'filterrn'
 ,p_source_type => 'HEADER'
 ,p_access_method => 'IN'
 ,p_param_type => 'LONG'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2832427241260571 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2831104685225737 + wwv_flow_api.g_id_offset
 ,p_name => 'session'
 ,p_bind_variable_name => 'session'
 ,p_source_type => 'HEADER'
 ,p_access_method => 'IN'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_template (
  p_id => 2846501644471933 + wwv_flow_api.g_id_offset
 ,p_module_id => 5622617744333346 + wwv_flow_api.g_id_offset
 ,p_uri_template => 'filter/delete/'
 ,p_priority => 0
 ,p_etag_type => 'HASH'
  );
 
wwv_flow_api.create_restful_handler (
  p_id => 2846609956474365 + wwv_flow_api.g_id_offset
 ,p_template_id => 2846501644471933 + wwv_flow_api.g_id_offset
 ,p_source_type => 'PLSQL'
 ,p_format => 'DEFAULT'
 ,p_method => 'POST'
 ,p_require_https => 'NO'
 ,p_source => 
'begin UDO_PKG_MOBILE_IFACE.DELETE_FILTER(:session,:filterrn,:error); end;'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2846710433484027 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2846609956474365 + wwv_flow_api.g_id_offset
 ,p_name => 'error'
 ,p_bind_variable_name => 'error'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_template (
  p_id => 2846121899440015 + wwv_flow_api.g_id_offset
 ,p_module_id => 5622617744333346 + wwv_flow_api.g_id_offset
 ,p_uri_template => 'filter/save/'
 ,p_priority => 0
 ,p_etag_type => 'HASH'
  );
 
wwv_flow_api.create_restful_handler (
  p_id => 2846202983443972 + wwv_flow_api.g_id_offset
 ,p_template_id => 2846121899440015 + wwv_flow_api.g_id_offset
 ,p_source_type => 'PLSQL'
 ,p_format => 'DEFAULT'
 ,p_method => 'POST'
 ,p_require_https => 'NO'
 ,p_source => 
'begin udo_pkg_mobile_iface.store_filter(:session,:filterrn,:filtername,:claimnumb,:claimvers,:claimrelease,:claimbuild,:claimunit,:claimapp,:claimiminit,:claimimperf,:claimcontent,:outrn,:error); end;'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2846401513462505 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2846202983443972 + wwv_flow_api.g_id_offset
 ,p_name => 'error'
 ,p_bind_variable_name => 'error'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2846327702460551 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2846202983443972 + wwv_flow_api.g_id_offset
 ,p_name => 'outrn'
 ,p_bind_variable_name => 'outrn'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'LONG'
  );
 
wwv_flow_api.create_restful_template (
  p_id => 2834406332595156 + wwv_flow_api.g_id_offset
 ,p_module_id => 5622617744333346 + wwv_flow_api.g_id_offset
 ,p_uri_template => 'filters/'
 ,p_priority => 0
 ,p_etag_type => 'HASH'
  );
 
wwv_flow_api.create_restful_handler (
  p_id => 2834529234630183 + wwv_flow_api.g_id_offset
 ,p_template_id => 2834406332595156 + wwv_flow_api.g_id_offset
 ,p_source_type => 'QUERY'
 ,p_format => 'DEFAULT'
 ,p_method => 'GET'
 ,p_require_https => 'NO'
 ,p_source => 
'select * from table(UDO_PKG_MOBILE_IFACE.FILTERS(:session))'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2834627288639043 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2834529234630183 + wwv_flow_api.g_id_offset
 ,p_name => 'session'
 ,p_bind_variable_name => 'session'
 ,p_source_type => 'HEADER'
 ,p_access_method => 'IN'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_template (
  p_id => 2819102396557341 + wwv_flow_api.g_id_offset
 ,p_module_id => 5622617744333346 + wwv_flow_api.g_id_offset
 ,p_uri_template => 'login/'
 ,p_priority => 0
 ,p_etag_type => 'HASH'
  );
 
wwv_flow_api.create_restful_handler (
  p_id => 2819214863560922 + wwv_flow_api.g_id_offset
 ,p_template_id => 2819102396557341 + wwv_flow_api.g_id_offset
 ,p_source_type => 'PLSQL'
 ,p_format => 'DEFAULT'
 ,p_method => 'POST'
 ,p_require_https => 'NO'
 ,p_source => 
'begin UDO_PKG_MOBILE_IFACE.LOGIN (:user,:pass,:P_SESSID,:P_PP, :P_ERROR); end;'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2820120628679093 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2819214863560922 + wwv_flow_api.g_id_offset
 ,p_name => 'ERROR'
 ,p_bind_variable_name => 'P_ERROR'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2819604952567546 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2819214863560922 + wwv_flow_api.g_id_offset
 ,p_name => 'PPP'
 ,p_bind_variable_name => 'P_PP'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'INT'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2819530794565589 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2819214863560922 + wwv_flow_api.g_id_offset
 ,p_name => 'SESSONID'
 ,p_bind_variable_name => 'P_SESSID'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_template (
  p_id => 2874632663963684 + wwv_flow_api.g_id_offset
 ,p_module_id => 5622617744333346 + wwv_flow_api.g_id_offset
 ,p_uri_template => 'logoff/'
 ,p_priority => 0
 ,p_etag_type => 'HASH'
  );
 
wwv_flow_api.create_restful_handler (
  p_id => 2874716518968472 + wwv_flow_api.g_id_offset
 ,p_template_id => 2874632663963684 + wwv_flow_api.g_id_offset
 ,p_source_type => 'PLSQL'
 ,p_format => 'DEFAULT'
 ,p_method => 'POST'
 ,p_require_https => 'NO'
 ,p_source => 
'begin UDO_PKG_MOBILE_IFACE.LOGOFF (:session); end;'
  );
 
wwv_flow_api.create_restful_template (
  p_id => 2869018889215238 + wwv_flow_api.g_id_offset
 ,p_module_id => 5622617744333346 + wwv_flow_api.g_id_offset
 ,p_uri_template => 'nextpoint/'
 ,p_priority => 0
 ,p_etag_type => 'HASH'
  );
 
wwv_flow_api.create_restful_handler (
  p_id => 2869110709222337 + wwv_flow_api.g_id_offset
 ,p_template_id => 2869018889215238 + wwv_flow_api.g_id_offset
 ,p_source_type => 'QUERY'
 ,p_format => 'DEFAULT'
 ,p_method => 'GET'
 ,p_require_https => 'NO'
 ,p_source => 
'select * from table(UDO_PKG_MOBILE_IFACE.NEXTPOINTS(:session,:rn))'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2873012901124992 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2869110709222337 + wwv_flow_api.g_id_offset
 ,p_name => 'rn'
 ,p_bind_variable_name => 'rn'
 ,p_source_type => 'HEADER'
 ,p_access_method => 'IN'
 ,p_param_type => 'LONG'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2872909438123991 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2869110709222337 + wwv_flow_api.g_id_offset
 ,p_name => 'session'
 ,p_bind_variable_name => 'session'
 ,p_source_type => 'HEADER'
 ,p_access_method => 'IN'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_handler (
  p_id => 2870502301913502 + wwv_flow_api.g_id_offset
 ,p_template_id => 2869018889215238 + wwv_flow_api.g_id_offset
 ,p_source_type => 'PLSQL'
 ,p_format => 'DEFAULT'
 ,p_method => 'POST'
 ,p_require_https => 'NO'
 ,p_source => 
'begin udo_pkg_mobile_iface.claim_forward(:session, :rn, :stat, :person, :note, :priority, :relfix, :bldfix, :error, :result); end;'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2870606803914815 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2870502301913502 + wwv_flow_api.g_id_offset
 ,p_name => 'error'
 ,p_bind_variable_name => 'error'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2870711998916260 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2870502301913502 + wwv_flow_api.g_id_offset
 ,p_name => 'result'
 ,p_bind_variable_name => 'result'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'INT'
  );
 
wwv_flow_api.create_restful_template (
  p_id => 2869420508357559 + wwv_flow_api.g_id_offset
 ,p_module_id => 5622617744333346 + wwv_flow_api.g_id_offset
 ,p_uri_template => 'nextsend/'
 ,p_priority => 0
 ,p_etag_type => 'HASH'
  );
 
wwv_flow_api.create_restful_handler (
  p_id => 2869500553361313 + wwv_flow_api.g_id_offset
 ,p_template_id => 2869420508357559 + wwv_flow_api.g_id_offset
 ,p_source_type => 'QUERY'
 ,p_format => 'DEFAULT'
 ,p_method => 'GET'
 ,p_require_https => 'NO'
 ,p_source => 
'select * from table(UDO_PKG_MOBILE_IFACE.WHONEXTSEND(:session,:rn,:point))'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2872804936122635 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2869500553361313 + wwv_flow_api.g_id_offset
 ,p_name => 'point'
 ,p_bind_variable_name => 'point'
 ,p_source_type => 'HEADER'
 ,p_access_method => 'IN'
 ,p_param_type => 'LONG'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2872700434121371 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2869500553361313 + wwv_flow_api.g_id_offset
 ,p_name => 'rn'
 ,p_bind_variable_name => 'rn'
 ,p_source_type => 'HEADER'
 ,p_access_method => 'IN'
 ,p_param_type => 'LONG'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2872627662119804 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2869500553361313 + wwv_flow_api.g_id_offset
 ,p_name => 'session'
 ,p_bind_variable_name => 'session'
 ,p_source_type => 'HEADER'
 ,p_access_method => 'IN'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_template (
  p_id => 2869917924820485 + wwv_flow_api.g_id_offset
 ,p_module_id => 5622617744333346 + wwv_flow_api.g_id_offset
 ,p_uri_template => 'return/'
 ,p_priority => 0
 ,p_etag_type => 'HASH'
  );
 
wwv_flow_api.create_restful_handler (
  p_id => 2870031430824394 + wwv_flow_api.g_id_offset
 ,p_template_id => 2869917924820485 + wwv_flow_api.g_id_offset
 ,p_source_type => 'QUERY'
 ,p_format => 'DEFAULT'
 ,p_method => 'GET'
 ,p_require_https => 'NO'
 ,p_source => 
'select * from table(UDO_PKG_MOBILE_IFACE.RETPOINTMESSAGE(:session,:rn))'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2872428076849180 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2870031430824394 + wwv_flow_api.g_id_offset
 ,p_name => 'rn'
 ,p_bind_variable_name => 'rn'
 ,p_source_type => 'HEADER'
 ,p_access_method => 'IN'
 ,p_param_type => 'LONG'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2872323228847740 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2870031430824394 + wwv_flow_api.g_id_offset
 ,p_name => 'session'
 ,p_bind_variable_name => 'session'
 ,p_source_type => 'HEADER'
 ,p_access_method => 'IN'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_handler (
  p_id => 2870800139931753 + wwv_flow_api.g_id_offset
 ,p_template_id => 2869917924820485 + wwv_flow_api.g_id_offset
 ,p_source_type => 'PLSQL'
 ,p_format => 'DEFAULT'
 ,p_method => 'POST'
 ,p_require_https => 'NO'
 ,p_source => 
'begin udo_pkg_mobile_iface.claim_return(:session, :rn, :note, :error, :result); end;'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2870903602932773 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2870800139931753 + wwv_flow_api.g_id_offset
 ,p_name => 'error'
 ,p_bind_variable_name => 'error'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2871008451934165 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2870800139931753 + wwv_flow_api.g_id_offset
 ,p_name => 'result'
 ,p_bind_variable_name => 'result'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'INT'
  );
 
wwv_flow_api.create_restful_template (
  p_id => 2869731299814835 + wwv_flow_api.g_id_offset
 ,p_module_id => 5622617744333346 + wwv_flow_api.g_id_offset
 ,p_uri_template => 'send/'
 ,p_priority => 0
 ,p_etag_type => 'HASH'
  );
 
wwv_flow_api.create_restful_handler (
  p_id => 2869811691818691 + wwv_flow_api.g_id_offset
 ,p_template_id => 2869731299814835 + wwv_flow_api.g_id_offset
 ,p_source_type => 'QUERY'
 ,p_format => 'DEFAULT'
 ,p_method => 'GET'
 ,p_require_https => 'NO'
 ,p_source => 
'select * from table(UDO_PKG_MOBILE_IFACE.WHOSEND(:session,:rn))'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2871928155953985 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2869811691818691 + wwv_flow_api.g_id_offset
 ,p_name => 'rn'
 ,p_bind_variable_name => 'rn'
 ,p_source_type => 'HEADER'
 ,p_access_method => 'IN'
 ,p_param_type => 'INT'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2871824346952952 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2869811691818691 + wwv_flow_api.g_id_offset
 ,p_name => 'session'
 ,p_bind_variable_name => 'session'
 ,p_source_type => 'HEADER'
 ,p_access_method => 'IN'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_handler (
  p_id => 2871119317946789 + wwv_flow_api.g_id_offset
 ,p_template_id => 2869731299814835 + wwv_flow_api.g_id_offset
 ,p_source_type => 'PLSQL'
 ,p_format => 'DEFAULT'
 ,p_method => 'POST'
 ,p_require_https => 'NO'
 ,p_source => 
'begin UDO_PKG_MOBILE_IFACE.CLAIM_SEND(:session, :rn, :person, :note, :error, :result); end;'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2871225897948635 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2871119317946789 + wwv_flow_api.g_id_offset
 ,p_name => 'error'
 ,p_bind_variable_name => 'error'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'STRING'
  );
 
wwv_flow_api.create_restful_param (
  p_id => 2871332477950527 + wwv_flow_api.g_id_offset
 ,p_handler_id => 2871119317946789 + wwv_flow_api.g_id_offset
 ,p_name => 'result'
 ,p_bind_variable_name => 'result'
 ,p_source_type => 'RESPONSE'
 ,p_access_method => 'OUT'
 ,p_param_type => 'INT'
  );
 
null;
 
end;
/

commit;
begin
execute immediate 'begin sys.dbms_session.set_nls( param => ''NLS_NUMERIC_CHARACTERS'', value => '''''''' || replace(wwv_flow_api.g_nls_numeric_chars,'''''''','''''''''''') || ''''''''); end;';
end;
/
set verify on
set feedback on
set define on
prompt  ...done
