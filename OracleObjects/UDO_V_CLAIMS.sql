--------------------------------------------------------
--  File created - среда-мая-13-2015   
--------------------------------------------------------
--------------------------------------------------------
--  DDL for View UDO_V_CLAIMS
--------------------------------------------------------

  CREATE OR REPLACE FORCE VIEW "UDO_V_CLAIMS" ("NRN", "NCOMPANY", "NCRN", "SEVENT_PREF", "SEVENT_NUMB", "NPRIORITY", "DREG_DATE", "DCHANGE_DATE", "DPLAN_DATE", "DFACT_DATE", "SEVENT_AUTHID", "NEVENT_TYPE", "SEVENT_TYPE", "NEVENT_STAT", "SEVENT_STAT", "NINIT_PERSON", "SINIT_PERSON", "SINIT_PERSON_AGNCODE", "SCHANGER", "NCLIENT_PERSON", "SCLIENT_PERSON", "SCLIENT_PERSON_AGNCODE", "NGROUP_SIGN", "SEXECUTOR", "NSEND_PERSON", "NSEND_DIVISION", "NEXPIRED", "DEXPIRE_DATE", "NCLOSED", "NREL_TO", "NBUILD_TO", "NREL_FROM", "NBUILD_FROM", "SVERS_FROM", "SREL_FROM", "SBUILD_FROM", "SVERS_TO", "SREL_TO", "SBUILD_TO2", "SBUILD_TO", "SMODULE", "SUNITCODE", "SUNITFUNC", "SEVENT_DESCR", "NHELPSIGN", "NEXISTDOC", "SLINKED_CLAIM", "SLINKDOC_TYPE", "SLINKDOC_PATH", "NSEND_TO_DEVELOPERS") AS 
  select
   T.RN                                                          as NRN,
   T.COMPANY                                                     as NCOMPANY,
   T.CRN                                                         as NCRN,
   T.EVENT_PREF                                                  as SEVENT_PREF,
   T.EVENT_NUMB                                                  as SEVENT_NUMB,
   T.PRIORITY                                                    as NPRIORITY,
   T.REG_DATE                                                    as DREG_DATE,
   T.CHANGE_DATE                                                 as DCHANGE_DATE,
   T.PLAN_DATE                                                   as DPLAN_DATE,
   T.FACT_DATE                                                   as DFACT_DATE,
   T.AUTHID                                                      as SEVENT_AUTHID,
   T.EVENT_TYPE                                                  as NEVENT_TYPE,
   ET.EVNTYPE_CODE                                               as SEVENT_TYPE,
   T.EVENT_STAT                                                  as NEVENT_STAT,
   TS.EVNSTAT_CODE                                               as SEVENT_STAT,
   T.INIT_PERSON                                                 as NINIT_PERSON,
   substr(F_CLNPERSONS_FORMAT_CODE(IP.COMPANY, IP.CODE), 0, 255) as SINIT_PERSON,
   IPA.AGNABBR                                                   as SINIT_PERSON_AGNCODE,
   UL.NAME                                                       as SCHANGER,
   T.CLIENT_PERSON                                               as NCLIENT_PERSON,
   substr(F_CLNPERSONS_FORMAT_CODE(CP.COMPANY, CP.CODE), 0, 255) as SCLIENT_PERSON,
   CPA.AGNABBR                                                   as SCLIENT_PERSON_AGNCODE,
   case
     when T.SEND_PERSON is not null then 0
     else 1
   end                                                           as NGROUP_SIGN,
   case
     when T.SEND_DIVISION is null then SPA.AGNABBR
     else IDEP.CODE
   end                                                           as SEXECUTOR,
   T.SEND_PERSON                                                 as NSEND_PERSON,
   T.SEND_DIVISION                                               as NSEND_DIVISION,
   case
     when T.CLOSED = 0
          and sysdate - T.EXPIRE_DATE > 0 then 3
     WHEN T.CLOSED = 0
          and T.EXPIRE_DATE - sysdate between 0 and 5 then 2
     when T.CLOSED = 0
          and T.EXPIRE_DATE - sysdate > 5 then 1
     else null
   end                                                           as NEXPIRED,
   T.EXPIRE_DATE                                                 as DEXPIRE_DATE,
   T.CLOSED                                                      as NCLOSED,
   T2.SOFTRELEASE                                                as NREL_TO,
	T2.SOFTBUILD                                                  as NBUILD_TO,
   T2.CUR_SOFTRELEASE                                            as NREL_FROM,
	T2.CUR_SOFTBUILD                                              as NBUILD_FROM,
	FR.SOFTVERSION                                                as SVERS_FROM,
   FR.RELNAME                                                    as SREL_FROM,
   nvl(trim(FB.NAME), FR.RELNAME)                                as SBUILD_FROM,
   TR.SOFTVERSION                                                as SVERS_TO,
   TR.RELNAME                                                    as SREL_TO,
   nvl(trim(TB.NAME), TR.RELNAME)                                as SBUILD_TO2,
   trim(TB.NAME)                                                 as SBUILD_TO,
   T2.ACODE                                                      as SMODULE,
   T2.UNITCODE                                                   as SUNITCODE,
   T2.FCODE                                                      as SUNITFUNC,
   T.EVENT_DESCR                                                 as SEVENT_DESCR,
	T2.HELPSIGN                                                   as NHELPSIGN,
   case
     when exists (
      select *
      from
       FILELINKSUNITS FUL
      where
       FUL.TABLE_PRN = T.RN) then 1
     else null
   end                                                           as NEXISTDOC,
     -- сервисные поля
   cast (null as varchar2(30))                                   as SLINKED_CLAIM,
	cast (null as varchar2(40))                                   as SLINKDOC_TYPE,
	cast (null as varchar2(255))                                  as SLINKDOC_PATH,
	cast (null as number(1))                                      as NSEND_TO_DEVELOPERS
FROM
    CLNEVENTS          T,
    CLNEVENTS_EXT      T2,
    CLNEVNTYPES        ET,
    USERLIST           UL,
    CLNEVNSTATS        TS,
    CLNEVNTYPSTS       ES,
    CLNPERSONS         IP,
    AGNLIST            IPA,
    CLNPERSONS         CP,
    AGNLIST            CPA,
    CLNPERSONS         SP,
    AGNLIST            SPA,
    V_INS_DEPARTMENT   IDEP,
    UDO_SOFTRELEASES   FR,
    UDO_SOFTBUILDS     FB,
    UDO_SOFTRELEASES   TR,
    UDO_SOFTBUILDS     TB
WHERE
         T.EVENT_TYPE       in (4440, 4412, 4424)
     and T.RN               = T2.PRN(+)
     and T.AUTHID           = UL.AUTHID
	  and T.EVENT_TYPE       = ET.RN
     and T.EVENT_STAT       = ES.RN
     and ES.EVENT_STATUS    = TS.RN
     and T.INIT_PERSON      = IP.RN(+)
     and IP.PERS_AGENT      = IPA.RN(+)
     and T.CLIENT_PERSON    = CP.RN(+)
     and CP.PERS_AGENT      = CPA.RN(+)
     and T.SEND_PERSON      = SP.RN(+)
     and SP.PERS_AGENT      = SPA.RN(+)
     and T.SEND_DIVISION    = IDEP.RN(+)
     and T2.CUR_SOFTBUILD   = FB.RN(+)
     and T2.CUR_SOFTRELEASE = FR.RN(+)
     and T2.SOFTBUILD       = TB.RN(+)
     and T2.SOFTRELEASE     = TR.RN(+)
     and exists (select *
                 from V_USERPRIV UP
                 where UP.UNITCODE = 'Udo_Claims'
                 and   UP.COMPANY  = COMPANY)
