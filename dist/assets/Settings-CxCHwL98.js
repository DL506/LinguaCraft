import{$n as e,Bt as t,Cn as n,F as r,Ft as i,Gt as a,K as o,Lt as s,Nn as c,P as l,Pt as u,Qn as d,R as f,Rt as p,Tn as m,Un as h,V as g,Xn as _,Y as v,Zn as y,_ as b,_n as x,an as S,b as C,dn as w,et as ee,in as T,ln as E,qn as D,rn as O,sn as k,un as A,y as j,zt as M}from"./light-TBGKFU-e.js";import{H as N,h as P,ot as F,q as I,rt as L,s as R,w as z,z as B}from"./request-BS4zoC18.js";import{t as te}from"./use-merged-state-Cqr2carY.js";import{t as ne}from"./Select-TnGD6F8K.js";import{t as V}from"./Input-CEDJiX-T.js";import{n as H,t as U}from"./RadioGroup-ClrcNSxQ.js";import{t as W}from"./RadioButton-AZqpwJUl.js";import{t as G}from"./InputNumber-C9b3e2w0.js";import{t as K}from"./_common-BnUKsVpJ.js";import{d as q,l as J}from"./user-Tu7ZhD_u.js";import{r as Y}from"./index-DfNiZMjF.js";import{t as X}from"./useDevice-bhXyx8S5.js";function Z(e){let{primaryColor:t,opacityDisabled:n,borderRadius:r,textColor3:i}=e;return{...K,iconColor:i,textColor:`white`,loadingColor:t,opacityDisabled:n,railColor:`rgba(0, 0, 0, .14)`,railColorActive:t,buttonBoxShadow:`0 1px 4px 0 rgba(0, 0, 0, 0.3), inset 0 0 1px 0 rgba(0, 0, 0, 0.05)`,buttonColor:`#FFF`,railBorderRadiusSmall:r,railBorderRadiusMedium:r,railBorderRadiusLarge:r,buttonBorderRadiusSmall:r,buttonBorderRadiusMedium:r,buttonBorderRadiusLarge:r,boxShadowFocus:`0 0 0 2px ${g(t,{alpha:.2})}`}}var re={name:`Switch`,common:f,self:Z},ie=i(`switch`,`
 height: var(--n-height);
 min-width: var(--n-width);
 vertical-align: middle;
 user-select: none;
 -webkit-user-select: none;
 display: inline-flex;
 outline: none;
 justify-content: center;
 align-items: center;
`,[s(`children-placeholder`,`
 height: var(--n-rail-height);
 display: flex;
 flex-direction: column;
 overflow: hidden;
 pointer-events: none;
 visibility: hidden;
 `),s(`rail-placeholder`,`
 display: flex;
 flex-wrap: none;
 `),s(`button-placeholder`,`
 width: calc(1.75 * var(--n-rail-height));
 height: var(--n-rail-height);
 `),i(`base-loading`,`
 position: absolute;
 top: 50%;
 left: 50%;
 transform: translateX(-50%) translateY(-50%);
 font-size: calc(var(--n-button-width) - 4px);
 color: var(--n-loading-color);
 transition: color .3s var(--n-bezier);
 `,[j({left:`50%`,top:`50%`,originalTransform:`translateX(-50%) translateY(-50%)`})]),s(`checked, unchecked`,`
 transition: color .3s var(--n-bezier);
 color: var(--n-text-color);
 box-sizing: border-box;
 position: absolute;
 white-space: nowrap;
 top: 0;
 bottom: 0;
 display: flex;
 align-items: center;
 line-height: 1;
 `),s(`checked`,`
 right: 0;
 padding-right: calc(1.25 * var(--n-rail-height) - var(--n-offset));
 `),s(`unchecked`,`
 left: 0;
 justify-content: flex-end;
 padding-left: calc(1.25 * var(--n-rail-height) - var(--n-offset));
 `),u(`&:focus`,[s(`rail`,`
 box-shadow: var(--n-box-shadow-focus);
 `)]),p(`round`,[s(`rail`,`border-radius: calc(var(--n-rail-height) / 2);`,[s(`button`,`border-radius: calc(var(--n-button-height) / 2);`)])]),M(`disabled`,[M(`icon`,[p(`rubber-band`,[p(`pressed`,[s(`rail`,[s(`button`,`max-width: var(--n-button-width-pressed);`)])]),s(`rail`,[u(`&:active`,[s(`button`,`max-width: var(--n-button-width-pressed);`)])]),p(`active`,[p(`pressed`,[s(`rail`,[s(`button`,`left: calc(100% - var(--n-offset) - var(--n-button-width-pressed));`)])]),s(`rail`,[u(`&:active`,[s(`button`,`left: calc(100% - var(--n-offset) - var(--n-button-width-pressed));`)])])])])])]),p(`active`,[s(`rail`,[s(`button`,`left: calc(100% - var(--n-button-width) - var(--n-offset))`)])]),s(`rail`,`
 overflow: hidden;
 height: var(--n-rail-height);
 min-width: var(--n-rail-width);
 border-radius: var(--n-rail-border-radius);
 cursor: pointer;
 position: relative;
 transition:
 opacity .3s var(--n-bezier),
 background .3s var(--n-bezier),
 box-shadow .3s var(--n-bezier);
 background-color: var(--n-rail-color);
 `,[s(`button-icon`,`
 color: var(--n-icon-color);
 transition: color .3s var(--n-bezier);
 font-size: calc(var(--n-button-height) - 4px);
 position: absolute;
 left: 0;
 right: 0;
 top: 0;
 bottom: 0;
 display: flex;
 justify-content: center;
 align-items: center;
 line-height: 1;
 `,[j()]),s(`button`,`
 align-items: center; 
 top: var(--n-offset);
 left: var(--n-offset);
 height: var(--n-button-height);
 width: var(--n-button-width-pressed);
 max-width: var(--n-button-width);
 border-radius: var(--n-button-border-radius);
 background-color: var(--n-button-color);
 box-shadow: var(--n-button-box-shadow);
 box-sizing: border-box;
 cursor: inherit;
 content: "";
 position: absolute;
 transition:
 background-color .3s var(--n-bezier),
 left .3s var(--n-bezier),
 opacity .3s var(--n-bezier),
 max-width .3s var(--n-bezier),
 box-shadow .3s var(--n-bezier);
 `)]),p(`active`,[s(`rail`,`background-color: var(--n-rail-color-active);`)]),p(`loading`,[s(`rail`,`
 cursor: wait;
 `)]),p(`disabled`,[s(`rail`,`
 cursor: not-allowed;
 opacity: .5;
 `)])]),ae=[`aria-checked`,`tabindex`,`onClick`,`onFocus`,`onBlur`,`onKeyup`,`onKeydown`],oe={...l.props,size:String,value:{type:[String,Number,Boolean],default:void 0},loading:Boolean,defaultValue:{type:[String,Number,Boolean],default:!1},disabled:{type:Boolean,default:void 0},round:{type:Boolean,default:!0},"onUpdate:value":[Function,Array],onUpdateValue:[Function,Array],checkedValue:{type:[String,Number,Boolean],default:!0},uncheckedValue:{type:[String,Number,Boolean],default:!1},railStyle:Function,rubberBand:{type:Boolean,default:!0},spinProps:Object,onChange:[Function,Array]},Q,se=w({name:`Switch`,props:oe,slots:Object,setup(e){Q===void 0&&(Q=typeof CSS<`u`?CSS.supports!==void 0&&CSS.supports(`width`,`max(1px)`):!0);let{mergedClsPrefixRef:n,inlineThemeDisabled:i,mergedComponentPropsRef:a}=ee(e),o=l(`Switch`,`-switch`,ie,re,e,n),s=z(e,{mergedSize(t){return e.size===void 0?t?t.mergedSize.value:a?.value?.Switch?.size||`medium`:e.size}}),{mergedSizeRef:c,mergedDisabledRef:u}=s,d=h(e.defaultValue),f=D(e,`value`),p=te(f,d),m=O(()=>p.value===e.checkedValue),g=h(!1),_=h(!1),v=O(()=>{let{railStyle:t}=e;if(t)return t({focused:_.value,checked:m.value})});function y(t){let{"onUpdate:value":n,onChange:r,onUpdateValue:i}=e,{nTriggerFormInput:a,nTriggerFormChange:o}=s;n&&I(n,t),i&&I(i,t),r&&I(r,t),d.value=t,a(),o()}function b(){let{nTriggerFormFocus:e}=s;e()}function x(){let{nTriggerFormBlur:e}=s;e()}function S(){e.loading||u.value||(p.value===e.checkedValue?y(e.uncheckedValue):y(e.checkedValue))}function C(){_.value=!0,b()}function w(){_.value=!1,x(),g.value=!1}function T(t){e.loading||u.value||t.key===` `&&(p.value===e.checkedValue?y(e.uncheckedValue):y(e.checkedValue),g.value=!1)}function E(t){e.loading||u.value||t.key===` `&&(t.preventDefault(),g.value=!0)}let k=O(()=>{let{value:e}=c,{self:{opacityDisabled:n,railColor:r,railColorActive:i,buttonBoxShadow:a,buttonColor:s,boxShadowFocus:l,loadingColor:u,textColor:d,iconColor:f,[t(`buttonHeight`,e)]:p,[t(`buttonWidth`,e)]:m,[t(`buttonWidthPressed`,e)]:h,[t(`railHeight`,e)]:g,[t(`railWidth`,e)]:_,[t(`railBorderRadius`,e)]:v,[t(`buttonBorderRadius`,e)]:y},common:{cubicBezierEaseInOut:b}}=o.value,x,S,C;return Q?(x=`calc((${g} - ${p}) / 2)`,S=`max(${g}, ${p})`,C=`max(${_}, calc(${_} + ${p} - ${g}))`):(x=F((L(g)-L(p))/2),S=F(Math.max(L(g),L(p))),C=L(g)>L(p)?_:F(L(_)+L(p)-L(g))),{"--n-bezier":b,"--n-button-border-radius":y,"--n-button-box-shadow":a,"--n-button-color":s,"--n-button-width":m,"--n-button-width-pressed":h,"--n-button-height":p,"--n-height":S,"--n-offset":x,"--n-opacity-disabled":n,"--n-rail-border-radius":v,"--n-rail-color":r,"--n-rail-color-active":i,"--n-rail-height":g,"--n-rail-width":_,"--n-width":C,"--n-box-shadow-focus":l,"--n-loading-color":u,"--n-text-color":d,"--n-icon-color":f}}),A=i?r(`switch`,O(()=>c.value[0]),k,e):void 0;return{handleClick:S,handleBlur:w,handleFocus:C,handleKeyup:T,handleKeydown:E,mergedRailStyle:v,pressed:g,mergedClsPrefix:n,mergedValue:p,checked:m,mergedDisabled:u,cssVars:i?void 0:k,themeClass:A?.themeClass,onRender:A?.onRender}},render(){let{mergedClsPrefix:e,mergedDisabled:t,checked:n,mergedRailStyle:r,onRender:i,$slots:a}=this;i?.();let{checked:s,unchecked:c,icon:l,"checked-icon":u,"unchecked-icon":f}=a,p=!(B(l)&&B(u)&&B(f));return m(),k(`div`,{role:`switch`,"aria-checked":n,class:o([`${e}-switch`,this.themeClass,p&&`${e}-switch--icon`,n&&`${e}-switch--active`,t&&`${e}-switch--disabled`,this.round&&`${e}-switch--round`,this.loading&&`${e}-switch--loading`,this.pressed&&`${e}-switch--pressed`,this.rubberBand&&`${e}-switch--rubber-band`]),tabindex:this.mergedDisabled?void 0:0,style:d(this.cssVars),onClick:this.handleClick,onFocus:this.handleFocus,onBlur:this.handleBlur,onKeyup:this.handleKeyup,onKeydown:this.handleKeydown},[T(`div`,{class:o(`${e}-switch__rail`),"aria-hidden":`true`,style:d(r)},[v(()=>N(s,t=>N(c,n=>t||n?(m(),k(`div`,{key:4,"aria-hidden":!0,class:o(`${e}-switch__children-placeholder`)},[T(`div`,{class:o(`${e}-switch__rail-placeholder`)},[T(`div`,{class:o(`${e}-switch__button-placeholder`)},null,2),v(()=>t)],2),T(`div`,{class:o(`${e}-switch__rail-placeholder`)},[T(`div`,{class:o(`${e}-switch__button-placeholder`)},null,2),v(()=>n)],2)],2)):null))),T(`div`,{class:o(`${e}-switch__button`)},[v(()=>N(l,t=>N(u,n=>N(f,r=>(m(),S(C,null,{default:()=>this.loading?(m(),S(b,x({key:`loading`,clsPrefix:e,strokeWidth:20},this.spinProps),null,16,[`clsPrefix`])):this.checked&&(n||t)?(m(),k(`div`,{class:o(`${e}-switch__button-icon`),key:n?`checked-icon`:`icon`},[v(()=>n||t)],2)):!this.checked&&(r||t)?(m(),k(`div`,{class:o(`${e}-switch__button-icon`),key:r?`unchecked-icon`:`icon`},[v(()=>r||t)],2)):null},1024)))))),v(()=>N(s,t=>t&&(m(),k(`div`,{key:`checked`,class:o(`${e}-switch__checked`)},[v(()=>t)],2)))),v(()=>N(c,t=>t&&(m(),k(`div`,{key:`unchecked`,class:o(`${e}-switch__unchecked`)},[v(()=>t)],2))))],2)],6)],46,ae)}}),ce={class:`settings-page`},le={class:`setting-row`},ue={class:`setting-row__control`},de={class:`setting-row`},fe={class:`setting-row__control`},pe={class:`setting-row`},$={class:`setting-row__control`},me={class:`setting-row__hint`},he={class:`setting-row`},ge={class:`setting-row__control`},_e={class:`setting-row`},ve={class:`setting-row__control`},ye={class:`setting-row`},be={class:`setting-row__control`},xe={class:`setting-row`},Se={class:`setting-row__control`},Ce={class:`settings-page__actions`},we=a(w({__name:`Settings`,setup(t){let r=R(),{isMobile:i}=X(),a=Y(),o=h(!1),s=h({dailyWordGoal:50,dailyReadingGoal:1,accent:`us`,grammarFillCaseSensitive:!1,gradingMethod:`ai`,reminderTime:``});function l(e){a.setThemeMode(e)}async function u(){o.value=!0;try{await q({...s.value}),r.success(`设置已保存`)}catch{}finally{o.value=!1}}return n(async()=>{let e=await J();s.value={...e,reminderTime:e.reminderTime??``}}),(t,n)=>(m(),k(`div`,ce,[T(`div`,{class:y([`settings-page__grid`,{"is-mobile":_(i)}])},[T(`div`,le,[n[9]||=T(`span`,{class:`setting-row__label`},`主题模式`,-1),T(`div`,ue,[A(_(U),{value:_(a).themeMode,"onUpdate:value":l},{default:c(()=>[A(_(W),{value:`system`},{default:c(()=>[...n[6]||=[E(`跟随系统`,-1)]]),_:1}),A(_(W),{value:`light`},{default:c(()=>[...n[7]||=[E(`亮色`,-1)]]),_:1}),A(_(W),{value:`dark`},{default:c(()=>[...n[8]||=[E(`暗色`,-1)]]),_:1})]),_:1},8,[`value`])])]),T(`div`,de,[n[11]||=T(`span`,{class:`setting-row__label`},`语法填空区分大小写`,-1),T(`div`,fe,[A(_(se),{value:s.value.grammarFillCaseSensitive,"onUpdate:value":n[0]||=e=>s.value.grammarFillCaseSensitive=e},null,8,[`value`]),n[10]||=T(`span`,{class:`setting-row__hint`},`关闭后 unfortunately 与 Unfortunately 视为相同`,-1)])]),T(`div`,pe,[n[14]||=T(`span`,{class:`setting-row__label`},`批改方式`,-1),T(`div`,$,[A(_(U),{value:s.value.gradingMethod,"onUpdate:value":n[1]||=e=>s.value.gradingMethod=e},{default:c(()=>[A(_(H),{value:`ai`},{default:c(()=>[...n[12]||=[E(`AI 批改`,-1)]]),_:1}),A(_(H),{value:`manual`},{default:c(()=>[...n[13]||=[E(`人工批改`,-1)]]),_:1})]),_:1},8,[`value`]),T(`span`,me,e(s.value.gradingMethod===`ai`?`提交后即时出结果,含判分和解析`:`稍后出结果,选择题优先数据库比对,作文由真人批改`),1)])]),T(`div`,he,[n[16]||=T(`span`,{class:`setting-row__label`},`每日背单词目标`,-1),T(`div`,ge,[A(_(G),{value:s.value.dailyWordGoal,"onUpdate:value":n[2]||=e=>s.value.dailyWordGoal=e,min:5,max:300,step:5},null,8,[`value`]),n[15]||=T(`span`,{class:`setting-row__hint`},`词`,-1)])]),T(`div`,_e,[n[18]||=T(`span`,{class:`setting-row__label`},`每日阅读目标`,-1),T(`div`,ve,[A(_(G),{value:s.value.dailyReadingGoal,"onUpdate:value":n[3]||=e=>s.value.dailyReadingGoal=e,min:1,max:10},null,8,[`value`]),n[17]||=T(`span`,{class:`setting-row__hint`},`篇`,-1)])]),T(`div`,ye,[n[19]||=T(`span`,{class:`setting-row__label`},`发音口音`,-1),T(`div`,be,[A(_(ne),{value:s.value.accent,"onUpdate:value":n[4]||=e=>s.value.accent=e,class:`setting-row__select`,options:[{label:`美式发音`,value:`us`},{label:`英式发音`,value:`uk`}]},null,8,[`value`])])]),T(`div`,xe,[n[21]||=T(`span`,{class:`setting-row__label`},`学习提醒时间`,-1),T(`div`,Se,[A(_(V),{value:s.value.reminderTime,"onUpdate:value":n[5]||=e=>s.value.reminderTime=e,class:`setting-row__time`,placeholder:`HH:mm`},null,8,[`value`]),n[20]||=T(`span`,{class:`setting-row__hint`},`如 20:00,留空表示不提醒`,-1)])])],2),T(`div`,Ce,[A(_(P),{type:`primary`,loading:o.value,onClick:u},{default:c(()=>[...n[22]||=[E(`保存设置`,-1)]]),_:1},8,[`loading`])])]))}}),[[`__scopeId`,`data-v-6b4a599f`]]);export{we as default};