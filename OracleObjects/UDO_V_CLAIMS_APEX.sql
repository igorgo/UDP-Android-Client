--------------------------------------------------------
--  File created - ñðåäà-ìàÿ-13-2015   
--------------------------------------------------------
--------------------------------------------------------
--  DDL for View UDO_V_CLAIMS_APEX
--------------------------------------------------------

  CREATE OR REPLACE FORCE VIEW "UDO_V_CLAIMS_APEX" ("NRN", "SEVENT_TYPE", "SEVENT_TYPE_CLASS", "SREL", "SREL_COLOR", "SREL_TITLE", "SEVENT_NUMB", "DREG_DATE", "DREG_DATE_SHORT", "SINIT_PERSON_AGNCODE", "SDOC_ICON", "SUNITCODE", "SMODULE", "NPRIORITY", "SPRIORITY_CLASS", "SEVENT_STAT", "DCHANGE_DATE", "SEXECUTOR", "SEXECUTOR_CLASS", "DDCHANGE_DATE") AS 
  select C.NRN,
     C.SEVENT_TYPE,
       case C.SEVENT_TYPE
         when 'ÄÎÐÀÁÎÒÊÀ' then
          'rework'
         when 'ÇÀÌÅ×ÀÍÈÅ' then
          'rebuke'
         when 'ÎØÈÁÊÀ' then
          'error'
         else
          ''
       end as SEVENT_TYPE_CLASS,
       COALESCE(C.SBUILD_TO2, C.SREL_FROM) as SREL,
       case
         when C.SBUILD_TO2 is null then
          'g'
         else
          'b'
       end as SREL_COLOR,
       case
         when C.SBUILD_TO2 is null then
          'Ðåëèç îáíàðóæåíèÿ'
         else
          'Ðåëèç âêëþ÷åíèÿ'
       end as SREL_TITLE,
       C.SEVENT_NUMB,
       to_char(C.DREG_DATE,'DD.MM.YYYY HH24:MI') as DREG_DATE,
       to_char(C.DREG_DATE,'DD.MM.YYYY') as DREG_DATE_SHORT,
       C.SINIT_PERSON_AGNCODE,
       case
         when C.NEXISTDOC = 1 then
          'icon-ppaperclip'
         else
          ''
       end as SDOC_ICON,
       C.SUNITCODE,
       C.SMODULE,
       C.NPRIORITY,
     case
         when (C.NPRIORITY>5) then
          ' p_e_pror_r'
         when (C.NPRIORITY<5) then
          ' p_e_pror_g'
         else
          ' '
       end as SPRIORITY_CLASS,
     C.SEVENT_STAT,
     to_char(C.DCHANGE_DATE,'DD.MM.YYYY HH24:MI') as DCHANGE_DATE,
     case
      when C.SEXECUTOR!='Àðõèâ' then C.SEXECUTOR
      else ' '
     end as SEXECUTOR,
     case
      when C.SEXECUTOR!='Àðõèâ' and C.NGROUP_SIGN=0 then 'icon-puser'
      when C.SEXECUTOR!='Àðõèâ' and C.NGROUP_SIGN=1 then 'icon-pgroup'
      else 'phide'
     end as SEXECUTOR_CLASS,
     C.DCHANGE_DATE DDCHANGE_DATE
  from UDO_V_CLAIMS C
