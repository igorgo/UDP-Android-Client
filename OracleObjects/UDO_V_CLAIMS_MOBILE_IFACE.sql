--------------------------------------------------------
--  File created - среда-мая-13-2015   
--------------------------------------------------------
--------------------------------------------------------
--  DDL for View UDO_V_CLAIMS_MOBILE_IFACE
--------------------------------------------------------

  CREATE OR REPLACE FORCE VIEW "UDO_V_CLAIMS_MOBILE_IFACE" ("RN", "CHANGE_DATE", "EVENT_NUMB", "EVENT_TYPE", "REL_BLD_REL", "RELEASE_TO", "REL_FR_RN", "BLD_FR_RN", "REL_TO_RN", "BLD_TO_RN", "REG_DATE", "UNITCODE", "ACODE", "FCODE", "EVNSTAT_CODE", "STATUDPTYPE", "INITIATOR", "EVENT_DESCR", "PRIORITY", "EXECUTOR", "EXECUTOR_DEP", "NEXISTDOC") AS 
  select T.RN,
       T.CHANGE_DATE,
       trim(T.EVENT_NUMB) as EVENT_NUMB,
       T.EVENT_TYPE,
       COALESCE(TOB.NAME, TOR.RELNAME, FRR.RELNAME) as REL_BLD_REL,
       TOR.RELNAME as RELEASE_TO,
       FRR.RN REL_FR_RN,
       FRB.RN BLD_FR_RN,
       TOR.RN REL_TO_RN,
       TOB.RN BLD_TO_RN,
       T.REG_DATE,
       T2.UNITCODE,
       T2.ACODE,
       T2.FCODE,
       ST.EVNSTAT_CODE,
       ST.STATUDPTYPE,
       IPA.AGNABBR INITIATOR,
       T.EVENT_DESCR,
       T.PRIORITY,
       SPA.AGNABBR EXECUTOR,
       IDEP.CODE EXECUTOR_DEP,
       case
         when exists
          (select * from FILELINKSUNITS FUL where FUL.TABLE_PRN = T.RN) then
          1
         else
          0
       end NEXISTDOC
  from CLNEVENTS        T,
       CLNEVENTS_EXT    T2,
       UDO_SOFTRELEASES TOR,
       UDO_SOFTBUILDS   TOB,
       UDO_SOFTRELEASES FRR,
       UDO_SOFTBUILDS FRB,
       CLNEVNSTATS      ST,
       CLNEVNTYPSTS     EST,
       AGNLIST          IPA,
       CLNPERSONS       IP,
       CLNPERSONS       SP,
       AGNLIST          SPA,
       V_INS_DEPARTMENT IDEP
 where T2.PRN = T.RN
   and T2.SOFTBUILD = TOB.RN(+)
   and T2.SOFTRELEASE = TOR.RN(+)
   and T2.CUR_SOFTRELEASE = FRR.RN(+)
   and T2.CUR_SOFTBUILD = FRB.RN(+)
   and T.EVENT_STAT = EST.RN
   and EST.EVENT_STATUS = ST.RN
   and IP.PERS_AGENT = IPA.RN(+)
   and T.INIT_PERSON = IP.RN(+)
   and T.SEND_PERSON = SP.RN(+)
   and SP.PERS_AGENT = SPA.RN(+)
   and T.SEND_DIVISION = IDEP.RN(+)
