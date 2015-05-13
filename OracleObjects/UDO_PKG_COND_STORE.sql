CREATE OR REPLACE package UDO_PKG_COND_STORE is

  -- Author  : IGOR-GO
  -- Created : 03.04.2015 13:50:32
  -- Purpose : API хранимых серверных условий отбора

  /* запись уловий отбора */
  type T_COND is record(
    RN         UDO_COND_STORE.RN%type,
    STORE_NAME UDO_COND_STORE.STORE_NAME %type,
    EDITABLE   char(1));

  /* таблица с записями групп */
  type T_CONDS is table of T_COND;

  /* Поиск условий отбора по имени и группе*/
  function FIND_RN_BY_NAME
  (
    P_COND_GROUP in varchar2,
    P_STORE_NAME in varchar2
  ) return number;

  /* Поиск условий отбора по умолчанию в группе*/
  function GET_DAEFAULT_STORE(P_COND_GROUP in varchar2) return number;

  /* Поиск условий отбора по имени и группе*/
  function FIND_NAME_BY_RN(P_RN in number) return varchar2;

  function STRIP_CDATA(P_CDATA in clob) return clob;
  pragma restrict_references(STRIP_CDATA, wnds, rnps);

  /* представление */
  function V(P_COND_GROUP in varchar2) return T_CONDS
    pipelined;

  /* инициализация добавления/исправления */
  procedure PROLOGUE
  (
    P_RN         in number, -- если задан то исправление
    P_COND_GROUP in varchar2, -- для исправления можно не задавать
    P_STORE_NAME in varchar2 -- если пусто то временная
  );

  /* фиксация добавления/исправления */
  procedure EPILOGUE(P_RN out number);

  /* удаление группы отбора*/
  procedure DEL(P_RN in number);

  /* добавление значения (строка) */
  procedure ADD_VALUE
  (
    P_NAME  in varchar2,
    P_VALUE in varchar2
  );

  /* добавление значения (число) */
  procedure ADD_VALUE
  (
    P_NAME  in varchar2,
    P_VALUE in number
  );
  /* добавление значения (дата) */
  procedure ADD_VALUE
  (
    P_NAME  in varchar2,
    P_VALUE in date
  );

  /* добавление значения (перечисление) */
  procedure ADD_VALUE_ENUM
  (
    P_NAME      in varchar2,
    P_VALUE     in varchar2,
    P_ENUM_TYPE in number, -- тип перечисления 0-строка, 1-число, 2-дата
    P_DELIMETER in varchar2 default ';'
  );

  /* установка значения условий отбора в COND_BROKER */
  procedure SET_COND_TO_BROKER(P_RN in number);

  procedure GET_STORE_ATTR_VAL
  (
    P_RN        in number,
    P_ATTR_NAME in varchar2,
    P_VALUE     out varchar2
  );

  procedure GET_STORE_ATTR_VAL
  (
    P_RN        in number,
    P_ATTR_NAME in varchar2,
    P_VALUE     out number
  );

  procedure GET_STORE_ATTR_VAL
  (
    P_RN        in number,
    P_ATTR_NAME in varchar2,
    P_VALUE     out date
  );

end UDO_PKG_COND_STORE;

/


CREATE OR REPLACE package body UDO_PKG_COND_STORE is

  TEMP_COND_NAME    constant char(45) := '$TEMPORARY_COND_UPDATED_WHEN_NEW_ONE_INSERTS$';
  DATE_FORMATH      constant char(21) := 'DD.MM.YYYY HH24:MI:SS';
  DATE_FORMAT       constant char(10) := 'DD.MM.YYYY';
  DECIMAL_DELIMETER constant char(1) := '.';
  TAG_ROOT          constant char(4) := 'root';
  TAG_ITEM          constant char(4) := 'item';
  ATTR_NAME         constant char(4) := 'name';
  ATTR_TYPE         constant char(4) := 'type';
  ATTR_SUBTYPE      constant char(7) := 'subtype';
  ATTR_DELIMETER    constant char(9) := 'delimeter';
  TYPE_STRING       constant char(6) := 'string';
  TYPE_NUMBER       constant char(6) := 'number';
  TYPE_DATE         constant char(4) := 'date';
  TYPE_ENUM         constant char(4) := 'enum';

  G_DECIMAL_DELIMETER char(1);

  type T_STORE_REC is record(
    RN         UDO_COND_STORE.RN%type,
    COND_GROUP UDO_COND_STORE.COND_GROUP%type,
    STORE_NAME UDO_COND_STORE.STORE_NAME%type,
    XVALUES    clob);
  G_STORE_REC       T_STORE_REC;
  G_EMPTY_STORE_REC T_STORE_REC;

  /* Формирование имени группы отбора */
  function MAKE_NAME_
  (
    P_COND_GROUP in varchar2,
    P_STORE_NAME in varchar2
  ) return varchar2 is
    L_NAME   UDO_COND_STORE.STORE_NAME %type;
    L_TMP    UDO_COND_STORE.STORE_NAME %type;
    L_LENGHT binary_integer;
    L_COUNT  binary_integer := 0;
    L_ITER   binary_integer := 0;
  begin
    L_LENGHT := 160;
    L_NAME   := SUBSTR(P_STORE_NAME, 1, L_LENGHT);
    L_TMP    := L_NAME;
    loop
      if L_COUNT > 0 then
        L_TMP  := SUBSTR(L_NAME, 1, LENGTH(L_NAME) - 1 - LENGTH(L_ITER));
        L_NAME := L_TMP || '~' || L_ITER;
        L_ITER := L_ITER + 1;
      end if;
    
      select count(*)
        into L_COUNT
        from UDO_COND_STORE T
       where authid = UTILIZER
         and COND_GROUP = P_COND_GROUP
         and STORE_NAME = L_NAME;
      exit when L_COUNT <= 0;
    end loop;
    return L_NAME;
  end;

  /* Поиск условий отбора по имени и группе*/
  function FIND_NAME_BY_RN(P_RN in number) return varchar2 is
    L_RESULT UDO_COND_STORE.STORE_NAME%type;
    cursor LC_REC is
      select STORE_NAME
        from UDO_COND_STORE
       where RN = P_RN
         and authid = UTILIZER;
  begin
    open LC_REC;
    fetch LC_REC
      into L_RESULT;
    close LC_REC;
    return L_RESULT;
  end;

  /* Поиск условий отбора по имени и группе*/
  function FIND_RN_BY_NAME
  (
    P_COND_GROUP in varchar2,
    P_STORE_NAME in varchar2
  ) return number is
    L_RESULT PKG_STD.TREF;
    cursor LC_REC is
      select RN
        from UDO_COND_STORE
       where COND_GROUP = P_COND_GROUP
         and STORE_NAME = P_STORE_NAME
         and authid = UTILIZER
      union
      select RN
        from UDO_COND_STORE
       where COND_GROUP = P_COND_GROUP
         and STORE_NAME = P_STORE_NAME
         and ROLE in (select R.ROLEID
                        from USERROLES R
                       where R.AUTHID = UTILIZER);
  begin
    open LC_REC;
    fetch LC_REC
      into L_RESULT;
    close LC_REC;
    return L_RESULT;
  end;

  /* Поиск условий отбора по умолчанию в группе*/
  function GET_DAEFAULT_STORE(P_COND_GROUP in varchar2) return number is
    L_RESULT PKG_STD.TREF;
    cursor LC_REC is
      select RN
        from UDO_COND_STORE
       where COND_GROUP = P_COND_GROUP
         and STORE_NAME = TEMP_COND_NAME
         and authid = UTILIZER;
  begin
    open LC_REC;
    fetch LC_REC
      into L_RESULT;
    close LC_REC;
    return L_RESULT;
  end;

  /* представление */
  function V(P_COND_GROUP in varchar2) return T_CONDS
    pipelined is
    cursor LC_CONDS is
      select RN,
             STORE_NAME,
             'Y'
        from UDO_COND_STORE
       where COND_GROUP = P_COND_GROUP
         and STORE_NAME != TEMP_COND_NAME
         and authid = UTILIZER
      union
      select distinct RN,
             STORE_NAME,
             'N'
        from UDO_COND_STORE
       where COND_GROUP = P_COND_GROUP
         and ROLE in (select R.ROLEID
                        from USERROLES R
                       where R.AUTHID = UTILIZER);
    L_COND T_COND;
  begin
    open LC_CONDS;
    loop
      fetch LC_CONDS
        into L_COND;
      exit when LC_CONDS%notfound;
      pipe row(L_COND);
    end loop;
    close LC_CONDS;
  end;

  /* считывание записи */
  function READ_(P_RN in number) return UDO_COND_STORE%rowtype is
    cursor LC_REC is
      select *
        from UDO_COND_STORE
       where RN = P_RN
         and authid = UTILIZER
      union all
      select *
        from UDO_COND_STORE
       where RN = P_RN
         and ROLE in (select R.ROLEID
                        from USERROLES R
                       where R.AUTHID = UTILIZER);
    L_REC LC_REC%rowtype;
  begin
    open LC_REC;
    fetch LC_REC
      into L_REC;
    close LC_REC;
    return L_REC;
  end;

  /* проверка наличия записи */
  function EXISTS_(P_RN in number) return boolean is
    L_REC UDO_COND_STORE%rowtype;
  begin
    L_REC := READ_(P_RN);
    return L_REC.RN is not null;
  end;

  /* базовое добавление группы отбора */
  procedure BASE_INSERT_
  (
    P_COND_GROUP in varchar2,
    P_STORE_NAME in varchar2,
    P_VALUES     in clob,
    P_RN         out number
  ) is
  begin
    P_RN := GEN_ID;
    insert into UDO_COND_STORE
      (RN, COND_GROUP, STORE_NAME, authid, ROLE, XVALUES)
    values
      (P_RN, P_COND_GROUP, P_STORE_NAME, UTILIZER, null, XMLTYPE(P_VALUES));
  end;

  /* базовое исправление группы отбора */
  procedure BASE_UPDATE_
  (
    P_RN         in number,
    P_STORE_NAME in varchar2,
    P_VALUES     in clob
  ) is
  begin
    update UDO_COND_STORE
       set STORE_NAME = P_STORE_NAME,
           XVALUES    = XMLTYPE(P_VALUES)
     where RN = P_RN
       and authid = UTILIZER;
  end;

  /* базовое удаление */
  procedure BASE_DELETE_(P_RN in number) is
  begin
    delete UDO_COND_STORE
     where RN = P_RN
       and authid = UTILIZER;
  end;

  /* поиск/ добавление временной группы */
  function FIND_TEMP_RULE_(P_COND_GROUP in varchar2) return number is
    cursor LC_RULE is
      select RN
        from UDO_COND_STORE
       where authid = UTILIZER
         and COND_GROUP = P_COND_GROUP
         and STORE_NAME = TEMP_COND_NAME;
    L_RULE PKG_STD.TREF;
  begin
    open LC_RULE;
    fetch LC_RULE
      into L_RULE;
    close LC_RULE;
    return L_RULE;
  end;

  /* инициализация добавления/исправления */
  procedure PROLOGUE
  (
    P_RN         in number, -- если задан то исправление
    P_COND_GROUP in varchar2, -- для исправления можно не задавать
    P_STORE_NAME in varchar2 -- если пусто то временная
  ) is
    L_REC UDO_COND_STORE%rowtype;
  begin
    G_STORE_REC := G_EMPTY_STORE_REC;
    if P_RN is not null then
      if EXISTS_(P_RN) then
        L_REC                  := READ_(P_RN);
        G_STORE_REC.RN         := L_REC.RN;
        G_STORE_REC.COND_GROUP := L_REC.COND_GROUP;
        if P_STORE_NAME != L_REC.STORE_NAME then
          G_STORE_REC.STORE_NAME := MAKE_NAME_(L_REC.COND_GROUP, P_STORE_NAME);
        else
          G_STORE_REC.STORE_NAME := P_STORE_NAME;
        end if;
      else
        PKG_MSG.RECORD_NOT_FOUND(NFLAG_SMART => 0, NDOCUMENT => P_RN);
      end if;
    else
      G_STORE_REC.COND_GROUP := P_COND_GROUP;
      if P_STORE_NAME is null then
        G_STORE_REC.RN         := FIND_TEMP_RULE_(P_COND_GROUP);
        G_STORE_REC.STORE_NAME := TEMP_COND_NAME;
      else
        G_STORE_REC.STORE_NAME := MAKE_NAME_(P_COND_GROUP, P_STORE_NAME);
      end if;
    end if;
  end;

  /* фиксация добавления/исправления */
  procedure EPILOGUE(P_RN out number) is
    L_RN     PKG_STD.TREF;
    L_VALUES clob;
  begin
    L_VALUES := '<' || TAG_ROOT || '>' || G_STORE_REC.XVALUES || '</' ||
                TAG_ROOT || '>';
    L_RN     := G_STORE_REC.RN;
    if L_RN is null then
      BASE_INSERT_(P_COND_GROUP => G_STORE_REC.COND_GROUP,
                   P_STORE_NAME => G_STORE_REC.STORE_NAME,
                   P_VALUES     => L_VALUES,
                   P_RN         => L_RN);
    else
      BASE_UPDATE_(P_RN         => L_RN,
                   P_STORE_NAME => G_STORE_REC.STORE_NAME,
                   P_VALUES     => L_VALUES);
    end if;
    P_RN := L_RN;
  end;

  /* удаление группы отбора*/
  procedure DEL(P_RN in number) is
  begin
    BASE_DELETE_(P_RN);
  end;

  function ATTR_
  (
    P_NAME  in varchar2,
    P_VALUE in varchar
  ) return varchar2 is
  begin
    return ' ' || P_NAME || '="' || P_VALUE || '"';
  end;

  function CDATA_(P_VALUE in varchar2) return varchar2 is
  begin
    return '<![CDATA[' || P_VALUE || ']]>';
  end;

  function N2C_(P_NUMB in number) return varchar2 is
  begin
    return replace(TO_CHAR(P_NUMB), G_DECIMAL_DELIMETER, DECIMAL_DELIMETER);
  end;

  function C2N_(P_NUMB in varchar2) return number is
  begin
    return TO_NUMBER(replace(P_NUMB, DECIMAL_DELIMETER, G_DECIMAL_DELIMETER));
  end;

  function D2C_
  (
    P_DATE      in date,
    P_WITH_TIME in number default 0
  ) return varchar2 is
  begin
    if P_WITH_TIME = 1 then
      return TO_CHAR(P_DATE, DATE_FORMATH);
    else
      return TO_CHAR(P_DATE, DATE_FORMAT);
    end if;
  end;

  function C2D_(P_DATE in varchar2) return date is
  begin
    return TO_DATE(P_DATE, DATE_FORMATH);
  end;

  /* добавление значения (строка) */
  procedure ADD_VALUE
  (
    P_NAME  in varchar2,
    P_VALUE in varchar2
  ) is
  begin
    if P_VALUE is not null then
      G_STORE_REC.XVALUES := G_STORE_REC.XVALUES || '<' || TAG_ITEM ||
                             ATTR_(ATTR_NAME, P_NAME) ||
                             ATTR_(ATTR_TYPE, TYPE_STRING) || '>' ||
                             CDATA_(P_VALUE) || '</' || TAG_ITEM || '>';
    end if;
  end;

  /* добавление значения (число) */
  procedure ADD_VALUE
  (
    P_NAME  in varchar2,
    P_VALUE in number
  ) is
  begin
    if P_VALUE is not null then
      G_STORE_REC.XVALUES := G_STORE_REC.XVALUES || '<' || TAG_ITEM ||
                             ATTR_(ATTR_NAME, P_NAME) ||
                             ATTR_(ATTR_TYPE, TYPE_NUMBER) || '>' ||
                             N2C_(P_VALUE) || '</' || TAG_ITEM || '>';
    end if;
  end;

  /* добавление значения (дата) */
  procedure ADD_VALUE
  (
    P_NAME  in varchar2,
    P_VALUE in date
  ) is
  begin
    if P_VALUE is not null then
      G_STORE_REC.XVALUES := G_STORE_REC.XVALUES || '<' || TAG_ITEM ||
                             ATTR_(ATTR_NAME, P_NAME) ||
                             ATTR_(ATTR_TYPE, TYPE_DATE) || '>' ||
                             D2C_(P_VALUE) || '</' || TAG_ITEM || '>';
    end if;
  end;

  /* добавление значения (перечисление) */
  procedure ADD_VALUE_ENUM
  (
    P_NAME      in varchar2,
    P_VALUE     in varchar2,
    P_ENUM_TYPE in number, -- тип перечисления 0-строка, 1-число, 2-дата
    P_DELIMETER in varchar2 default ';'
  ) is
    L_STYPE varchar2(6);
  begin
    if P_VALUE is not null then
      case P_ENUM_TYPE
        when 0 then
          L_STYPE := TYPE_STRING;
        when 1 then
          L_STYPE := TYPE_NUMBER;
        when 2 then
          L_STYPE := TYPE_DATE;
        else
          return;
      end case;
      G_STORE_REC.XVALUES := G_STORE_REC.XVALUES || '<' || TAG_ITEM || ' ' ||
                             ATTR_(ATTR_NAME, P_NAME) ||
                             ATTR_(ATTR_TYPE, TYPE_ENUM) ||
                             ATTR_(ATTR_SUBTYPE, L_STYPE) ||
                             ATTR_(ATTR_DELIMETER, P_DELIMETER) || '>' ||
                             CDATA_(P_VALUE) || '</' || TAG_ITEM || '>';
    end if;
  end;

  function STRIP_CDATA(P_CDATA in clob) return clob is
  begin
    return RTRIM(LTRIM(P_CDATA, '<![CDATA['), ']]>');
  end;

  procedure GET_STORE_ATTR_VAL
  (
    P_RN        in number,
    P_ATTR_NAME in varchar2,
    P_VALUE     out varchar2
  ) is
    cursor LC_VALUE is
      select STRIP_CDATA(value(Z).EXTRACT('/' || TAG_ITEM || '/text()')
                         .GETCLOBVAL()) SVAL
        from UDO_COND_STORE T,
             table(XMLSEQUENCE(EXTRACT(T.XVALUES,
                                       '/' || TAG_ROOT || '/' || TAG_ITEM))) Z
       where T.RN = P_RN
         and value(Z).EXTRACT('/' || TAG_ITEM ||'/@' || ATTR_NAME)
            .GETSTRINGVAL() = P_ATTR_NAME;
  begin
    open LC_VALUE;
    fetch LC_VALUE
      into P_VALUE;
    close LC_VALUE;
  end;

  procedure GET_STORE_ATTR_VAL
  (
    P_RN        in number,
    P_ATTR_NAME in varchar2,
    P_VALUE     out number
  ) is
    L_VALUE PKG_STD.TSTRING;
  begin
    GET_STORE_ATTR_VAL(P_RN, P_ATTR_NAME, L_VALUE);
    P_VALUE := C2N_(L_VALUE);
  end;

  procedure GET_STORE_ATTR_VAL
  (
    P_RN        in number,
    P_ATTR_NAME in varchar2,
    P_VALUE     out date
  ) is
    L_VALUE PKG_STD.TSTRING;
  begin
    GET_STORE_ATTR_VAL(P_RN, P_ATTR_NAME, L_VALUE);
    P_VALUE := C2D_(L_VALUE);
  end;

  /* установка значения условий отбора в COND_BROKER */
  procedure SET_COND_TO_BROKER(P_RN in number) is
    cursor LC_VALUES is
      select value(Z).EXTRACT('/' || TAG_ITEM || '/@' || ATTR_NAME)
             .GETSTRINGVAL() as SNAME,
             value(Z).EXTRACT('/' || TAG_ITEM || '/@' || ATTR_TYPE)
             .GETSTRINGVAL() as STYPE,
             value(Z).EXTRACT('/' || TAG_ITEM || '/@' || ATTR_SUBTYPE)
             .GETSTRINGVAL() as SSUBTYPE,
             value(Z).EXTRACT('/' || TAG_ITEM || '/@' || ATTR_DELIMETER)
             .GETSTRINGVAL() as SDELIM,
             STRIP_CDATA(value(Z).EXTRACT('/' || TAG_ITEM || '/text()')
                         .GETCLOBVAL()) SVAL
        from UDO_COND_STORE T,
             table(XMLSEQUENCE(EXTRACT(T.XVALUES,
                                       '/' || TAG_ROOT || '/' || TAG_ITEM))) Z
       where T.RN = P_RN;
    type T_VALUES is table of LC_VALUES%rowtype index by binary_integer;
    L_VALUES T_VALUES;
  begin
    open LC_VALUES;
    fetch LC_VALUES bulk collect
      into L_VALUES;
    close LC_VALUES;
    for I in 1 .. L_VALUES.COUNT loop
      case L_VALUES(I).STYPE
        when TYPE_STRING then
          PKG_COND_BROKER.SET_CONDITION_STR(L_VALUES(I).SNAME,
                                            L_VALUES(I).SVAL);
        when TYPE_NUMBER then
          PKG_COND_BROKER.SET_CONDITION_NUM(L_VALUES(I).SNAME,
                                            C2N_(L_VALUES(I).SVAL));
        when TYPE_DATE then
          PKG_COND_BROKER.SET_CONDITION_DATE(L_VALUES(I).SNAME,
                                             C2D_(L_VALUES(I).SVAL));
        when TYPE_ENUM then
          case L_VALUES(I).SSUBTYPE
            when TYPE_STRING then
              PKG_COND_BROKER.SET_CONDITION_ESTR(L_VALUES(I).SNAME,
                                                 L_VALUES(I).SVAL,
                                                 L_VALUES(I).SDELIM);
            when TYPE_NUMBER then
              PKG_COND_BROKER.SET_CONDITION_ENUM(L_VALUES(I).SNAME,
                                                 L_VALUES(I).SVAL,
                                                 L_VALUES(I).SDELIM);
            when TYPE_DATE then
              PKG_COND_BROKER.SET_CONDITION_EDATE(L_VALUES(I).SNAME,
                                                  L_VALUES(I).SVAL,
                                                  L_VALUES(I).SDELIM);
            else
              null;
          end case;
        else
          null;
      end case;
    end loop;
  end;

begin
  select SUBSTR(value, 1, 1)
    into G_DECIMAL_DELIMETER
    from NLS_SESSION_PARAMETERS
   where PARAMETER = 'NLS_NUMERIC_CHARACTERS';

end UDO_PKG_COND_STORE;

/
