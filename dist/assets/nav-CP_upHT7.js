import{$t as e,En as t,F as n,Ft as r,G as i,H as a,I as o,J as s,K as c,Lt as l,M as u,Mn as d,N as f,P as p,Pt as m,Qn as h,R as g,Rt as _,S as v,Tn as y,Un as b,Y as x,_n as S,an as C,cn as w,dn as T,et as E,g as D,hn as O,in as k,j as A,pn as j,qn as M,rn as N,rt as P,sn as F,x as I,z as ee,zt as L}from"./light-TBGKFU-e.js";import{E as R,R as z,W as B,ct as V,k as te,q as H}from"./request-BS4zoC18.js";import{o as ne,t as U}from"./create-G_I1UjLa.js";import{n as re}from"./dark-BLG8oW_z.js";import{t as W}from"./use-merged-state-Cqr2carY.js";import{t as ie}from"./use-compitable-CpPvjT_b.js";import{n as ae,t as oe}from"./Dropdown-Bjjh2e2a.js";import{t as G}from"./format-length-BwSo_EUP.js";import{t as se}from"./ChevronRight-CqswQINz.js";function ce(e){let{baseColor:t,textColor2:n,bodyColor:r,cardColor:i,dividerColor:o,actionColor:s,scrollbarColor:c,scrollbarColorHover:l,invertedColor:u}=e;return{textColor:n,textColorInverted:`#FFF`,color:r,colorEmbedded:s,headerColor:i,headerColorInverted:u,footerColor:s,footerColorInverted:u,headerBorderColor:o,headerBorderColorInverted:u,footerBorderColor:o,footerBorderColorInverted:u,siderBorderColor:o,siderBorderColorInverted:u,siderColor:i,siderColorInverted:u,siderToggleButtonBorder:`1px solid ${o}`,siderToggleButtonColor:t,siderToggleButtonIconColor:n,siderToggleButtonIconColorInverted:n,siderToggleBarColor:a(r,c),siderToggleBarColorHover:a(r,l),__invertScrollbar:`true`}}var K=f({name:`Layout`,common:g,peers:{Scrollbar:o},self:ce}),le=P(`n-layout-sider`),q={type:String,default:`static`},ue=r(`layout`,`
 color: var(--n-text-color);
 background-color: var(--n-color);
 box-sizing: border-box;
 position: relative;
 z-index: auto;
 flex: auto;
 overflow: hidden;
 transition:
 box-shadow .3s var(--n-bezier),
 background-color .3s var(--n-bezier),
 color .3s var(--n-bezier);
`,[r(`layout-scroll-container`,`
 overflow-x: hidden;
 box-sizing: border-box;
 height: 100%;
 `),_(`absolute-positioned`,`
 position: absolute;
 left: 0;
 right: 0;
 top: 0;
 bottom: 0;
 `)]),de={embedded:Boolean,position:q,nativeScrollbar:{type:Boolean,default:!0},scrollbarProps:Object,onScroll:Function,contentClass:String,contentStyle:{type:[String,Object],default:``},hasSider:Boolean,siderPlacement:{type:String,default:`left`}},fe=P(`n-layout`);function pe(e){return T({name:e?`LayoutContent`:`Layout`,props:{...p.props,...de},setup(e){let r=b(null),i=b(null),{mergedClsPrefixRef:a,inlineThemeDisabled:o}=E(e),s=p(`Layout`,`-layout`,ue,K,e,a);function c(t,n){if(e.nativeScrollbar){let{value:e}=r;e&&(n===void 0?e.scrollTo(t):e.scrollTo(t,n))}else{let{value:e}=i;e&&e.scrollTo(t,n)}}t(fe,e);let l=0,u=0,d=t=>{let n=t.target;l=n.scrollLeft,u=n.scrollTop,e.onScroll?.(t)};z(()=>{if(e.nativeScrollbar){let e=r.value;e&&(e.scrollTop=u,e.scrollLeft=l)}});let f={display:`flex`,flexWrap:`nowrap`,width:`100%`,flexDirection:`row`},m={scrollTo:c},h=N(()=>{let{common:{cubicBezierEaseInOut:t},self:n}=s.value;return{"--n-bezier":t,"--n-color":e.embedded?n.colorEmbedded:n.color,"--n-text-color":n.textColor}}),g=o?n(`layout`,N(()=>e.embedded?`e`:``),h,e):void 0;return{mergedClsPrefix:a,scrollableElRef:r,scrollbarInstRef:i,hasSiderStyle:f,mergedTheme:s,handleNativeElScroll:d,cssVars:o?void 0:h,themeClass:g?.themeClass,onRender:g?.onRender,...m}},render(){let{mergedClsPrefix:t,hasSider:n}=this;this.onRender?.();let r=n?this.hasSiderStyle:void 0,i=[this.themeClass,e&&`${t}-layout-content`,`${t}-layout`,`${t}-layout--${this.position}-positioned`];return y(),F(`div`,{class:c(i),style:h(this.cssVars)},[this.nativeScrollbar?(y(),F(`div`,{key:0,ref:`scrollableElRef`,class:c([`${t}-layout-scroll-container`,this.contentClass]),style:h([this.contentStyle,r]),onScroll:this.handleNativeElScroll},[x(()=>this.$slots.default?.())],46,[`onScroll`])):(y(),C(R,S({key:1},this.scrollbarProps,{onScroll:this.onScroll,ref:`scrollbarInstRef`,theme:this.mergedTheme.peers.Scrollbar,themeOverrides:this.mergedTheme.peerOverrides.Scrollbar,contentClass:this.contentClass,contentStyle:[this.contentStyle,r]}),s(this.$slots),1040,[`onScroll`,`theme`,`themeOverrides`,`contentClass`,`contentStyle`]))],6)}})}var me=pe(!1),he=pe(!0),ge=r(`layout-header`,`
 transition:
 color .3s var(--n-bezier),
 background-color .3s var(--n-bezier),
 box-shadow .3s var(--n-bezier),
 border-color .3s var(--n-bezier);
 box-sizing: border-box;
 width: 100%;
 background-color: var(--n-color);
 color: var(--n-text-color);
`,[_(`absolute-positioned`,`
 position: absolute;
 left: 0;
 right: 0;
 top: 0;
 `),_(`bordered`,`
 border-bottom: solid 1px var(--n-border-color);
 `)]),_e={position:q,inverted:Boolean,bordered:Boolean},ve=T({name:`LayoutHeader`,props:{...p.props,..._e},setup(e){let{mergedClsPrefixRef:t,inlineThemeDisabled:r}=E(e),i=p(`Layout`,`-layout-header`,ge,K,e,t),a=N(()=>{let{common:{cubicBezierEaseInOut:t},self:n}=i.value,r={"--n-bezier":t};return e.inverted?(r[`--n-color`]=n.headerColorInverted,r[`--n-text-color`]=n.textColorInverted,r[`--n-border-color`]=n.headerBorderColorInverted):(r[`--n-color`]=n.headerColor,r[`--n-text-color`]=n.textColor,r[`--n-border-color`]=n.headerBorderColor),r}),o=r?n(`layout-header`,N(()=>e.inverted?`a`:`b`),a,e):void 0;return{mergedClsPrefix:t,cssVars:r?void 0:a,themeClass:o?.themeClass,onRender:o?.onRender}},render(){let{mergedClsPrefix:e}=this;return this.onRender?.(),y(),F(`div`,{class:c([`${e}-layout-header`,this.themeClass,this.position&&`${e}-layout-header--${this.position}-positioned`,this.bordered&&`${e}-layout-header--bordered`]),style:h(this.cssVars)},[x(()=>this.$slots.default?.())],6)}}),ye=r(`layout-sider`,`
 flex-shrink: 0;
 box-sizing: border-box;
 position: relative;
 z-index: 1;
 color: var(--n-text-color);
 transition:
 color .3s var(--n-bezier),
 border-color .3s var(--n-bezier),
 min-width .3s var(--n-bezier),
 max-width .3s var(--n-bezier),
 transform .3s var(--n-bezier),
 background-color .3s var(--n-bezier);
 background-color: var(--n-color);
 display: flex;
 justify-content: flex-end;
`,[_(`bordered`,[l(`border`,`
 content: "";
 position: absolute;
 top: 0;
 bottom: 0;
 width: 1px;
 background-color: var(--n-border-color);
 transition: background-color .3s var(--n-bezier);
 `)]),l(`left-placement`,[_(`bordered`,[l(`border`,`
 right: 0;
 `)])]),_(`right-placement`,`
 justify-content: flex-start;
 `,[_(`bordered`,[l(`border`,`
 left: 0;
 `)]),_(`collapsed`,[r(`layout-toggle-button`,[r(`base-icon`,`
 transform: rotate(180deg);
 `)]),r(`layout-toggle-bar`,[m(`&:hover`,[l(`top`,{transform:`rotate(-12deg) scale(1.15) translateY(-2px)`}),l(`bottom`,{transform:`rotate(12deg) scale(1.15) translateY(2px)`})])])]),r(`layout-toggle-button`,`
 left: 0;
 transform: translateX(-50%) translateY(-50%);
 `,[r(`base-icon`,`
 transform: rotate(0);
 `)]),r(`layout-toggle-bar`,`
 left: -28px;
 transform: rotate(180deg);
 `,[m(`&:hover`,[l(`top`,{transform:`rotate(12deg) scale(1.15) translateY(-2px)`}),l(`bottom`,{transform:`rotate(-12deg) scale(1.15) translateY(2px)`})])])]),_(`collapsed`,[r(`layout-toggle-bar`,[m(`&:hover`,[l(`top`,{transform:`rotate(-12deg) scale(1.15) translateY(-2px)`}),l(`bottom`,{transform:`rotate(12deg) scale(1.15) translateY(2px)`})])]),r(`layout-toggle-button`,[r(`base-icon`,`
 transform: rotate(0);
 `)])]),r(`layout-toggle-button`,`
 transition:
 color .3s var(--n-bezier),
 right .3s var(--n-bezier),
 left .3s var(--n-bezier),
 border-color .3s var(--n-bezier),
 background-color .3s var(--n-bezier);
 cursor: pointer;
 width: 24px;
 height: 24px;
 position: absolute;
 top: 50%;
 right: 0;
 border-radius: 50%;
 display: flex;
 align-items: center;
 justify-content: center;
 font-size: 18px;
 color: var(--n-toggle-button-icon-color);
 border: var(--n-toggle-button-border);
 background-color: var(--n-toggle-button-color);
 box-shadow: 0 2px 4px 0px rgba(0, 0, 0, .06);
 transform: translateX(50%) translateY(-50%);
 z-index: 1;
 `,[r(`base-icon`,`
 transition: transform .3s var(--n-bezier);
 transform: rotate(180deg);
 `)]),r(`layout-toggle-bar`,`
 cursor: pointer;
 height: 72px;
 width: 32px;
 position: absolute;
 top: calc(50% - 36px);
 right: -28px;
 `,[l(`top, bottom`,`
 position: absolute;
 width: 4px;
 border-radius: 2px;
 height: 38px;
 left: 14px;
 transition: 
 background-color .3s var(--n-bezier),
 transform .3s var(--n-bezier);
 `),l(`bottom`,`
 position: absolute;
 top: 34px;
 `),m(`&:hover`,[l(`top`,{transform:`rotate(12deg) scale(1.15) translateY(-2px)`}),l(`bottom`,{transform:`rotate(-12deg) scale(1.15) translateY(2px)`})]),l(`top, bottom`,{backgroundColor:`var(--n-toggle-bar-color)`}),m(`&:hover`,[l(`top, bottom`,{backgroundColor:`var(--n-toggle-bar-color-hover)`})])]),l(`border`,`
 position: absolute;
 top: 0;
 right: 0;
 bottom: 0;
 width: 1px;
 transition: background-color .3s var(--n-bezier);
 `),r(`layout-sider-scroll-container`,`
 flex-grow: 1;
 flex-shrink: 0;
 box-sizing: border-box;
 height: 100%;
 opacity: 0;
 transition: opacity .3s var(--n-bezier);
 max-width: 100%;
 `),_(`show-content`,[r(`layout-sider-scroll-container`,{opacity:1})]),_(`absolute-positioned`,`
 position: absolute;
 left: 0;
 top: 0;
 bottom: 0;
 `)]),be=[`onClick`],xe=T({props:{clsPrefix:{type:String,required:!0},onClick:Function},render(){let{clsPrefix:e}=this;return y(),F(`div`,{onClick:this.onClick,class:c(`${e}-layout-toggle-bar`)},[k(`div`,{class:c(`${e}-layout-toggle-bar__top`)},null,2),k(`div`,{class:c(`${e}-layout-toggle-bar__bottom`)},null,2)],10,be)}}),Se=[`onClick`],Ce=T({name:`LayoutToggleButton`,props:{clsPrefix:{type:String,required:!0},onClick:Function},render(){let{clsPrefix:e}=this;return y(),F(`div`,{class:c(`${e}-layout-toggle-button`),onClick:this.onClick},[(y(),C(u,{clsPrefix:e},{default:()=>(y(),C(se))},1032,[`clsPrefix`]))],10,Se)}}),we=[`onTransitionend`],Te={position:q,bordered:Boolean,collapsedWidth:{type:Number,default:48},width:{type:[Number,String],default:272},contentClass:String,contentStyle:{type:[String,Object],default:``},collapseMode:{type:String,default:`transform`},collapsed:{type:Boolean,default:void 0},defaultCollapsed:Boolean,showCollapsedContent:{type:Boolean,default:!0},showTrigger:{type:[Boolean,String],default:!1},nativeScrollbar:{type:Boolean,default:!0},inverted:Boolean,scrollbarProps:Object,triggerClass:String,triggerStyle:[String,Object],collapsedTriggerClass:String,collapsedTriggerStyle:[String,Object],"onUpdate:collapsed":[Function,Array],onUpdateCollapsed:[Function,Array],onAfterEnter:Function,onAfterLeave:Function,onExpand:[Function,Array],onCollapse:[Function,Array],onScroll:Function},Ee=T({name:`LayoutSider`,props:{...p.props,...Te},setup(e){let r=O(fe),i=b(null),a=b(null),o=b(e.defaultCollapsed),s=W(M(e,`collapsed`),o),c=N(()=>G(s.value?e.collapsedWidth:e.width)),l=N(()=>e.collapseMode===`transform`?{minWidth:G(e.width)}:{}),u=N(()=>r?r.siderPlacement:`left`);function d(t,n){if(e.nativeScrollbar){let{value:e}=i;e&&(n===void 0?e.scrollTo(t):e.scrollTo(t,n))}else{let{value:e}=a;e&&e.scrollTo(t,n)}}function f(){let{"onUpdate:collapsed":t,onUpdateCollapsed:n,onExpand:r,onCollapse:i}=e,{value:a}=s;n&&H(n,!a),t&&H(t,!a),o.value=!a,a?r&&H(r):i&&H(i)}let m=0,h=0,g=t=>{let n=t.target;m=n.scrollLeft,h=n.scrollTop,e.onScroll?.(t)};z(()=>{if(e.nativeScrollbar){let e=i.value;e&&(e.scrollTop=h,e.scrollLeft=m)}}),t(le,{collapsedRef:s,collapseModeRef:M(e,`collapseMode`)});let{mergedClsPrefixRef:_,inlineThemeDisabled:v}=E(e),y=p(`Layout`,`-layout-sider`,ye,K,e,_);function x(t){t.propertyName===`max-width`&&(s.value?e.onAfterLeave?.():e.onAfterEnter?.())}let S={scrollTo:d},C=N(()=>{let{common:{cubicBezierEaseInOut:t},self:n}=y.value,{siderToggleButtonColor:r,siderToggleButtonBorder:i,siderToggleBarColor:a,siderToggleBarColorHover:o}=n,s={"--n-bezier":t,"--n-toggle-button-color":r,"--n-toggle-button-border":i,"--n-toggle-bar-color":a,"--n-toggle-bar-color-hover":o};return e.inverted?(s[`--n-color`]=n.siderColorInverted,s[`--n-text-color`]=n.textColorInverted,s[`--n-border-color`]=n.siderBorderColorInverted,s[`--n-toggle-button-icon-color`]=n.siderToggleButtonIconColorInverted,s.__invertScrollbar=n.__invertScrollbar):(s[`--n-color`]=n.siderColor,s[`--n-text-color`]=n.textColor,s[`--n-border-color`]=n.siderBorderColor,s[`--n-toggle-button-icon-color`]=n.siderToggleButtonIconColor),s}),w=v?n(`layout-sider`,N(()=>e.inverted?`a`:`b`),C,e):void 0;return{scrollableElRef:i,scrollbarInstRef:a,mergedClsPrefix:_,mergedTheme:y,styleMaxWidth:c,mergedCollapsed:s,scrollContainerStyle:l,siderPlacement:u,handleNativeElScroll:g,handleTransitionend:x,handleTriggerClick:f,inlineThemeDisabled:v,cssVars:C,themeClass:w?.themeClass,onRender:w?.onRender,...S}},render(){let{mergedClsPrefix:t,mergedCollapsed:n,showTrigger:r}=this;return this.onRender?.(),y(),F(`aside`,{class:c([`${t}-layout-sider`,this.themeClass,`${t}-layout-sider--${this.position}-positioned`,`${t}-layout-sider--${this.siderPlacement}-placement`,this.bordered&&`${t}-layout-sider--bordered`,n&&`${t}-layout-sider--collapsed`,(!n||this.showCollapsedContent)&&`${t}-layout-sider--show-content`]),onTransitionend:this.handleTransitionend,style:h([this.inlineThemeDisabled?void 0:this.cssVars,{maxWidth:this.styleMaxWidth,width:G(this.width)}])},[this.nativeScrollbar?(y(),F(`div`,{key:1,class:c([`${t}-layout-sider-scroll-container`,this.contentClass]),onScroll:this.handleNativeElScroll,style:h([this.scrollContainerStyle,{overflow:`auto`},this.contentStyle]),ref:`scrollableElRef`},[x(()=>this.$slots.default?.())],46,[`onScroll`])):(y(),C(R,S({key:0},this.scrollbarProps,{onScroll:this.onScroll,ref:`scrollbarInstRef`,style:this.scrollContainerStyle,contentStyle:this.contentStyle,contentClass:this.contentClass,theme:this.mergedTheme.peers.Scrollbar,themeOverrides:this.mergedTheme.peerOverrides.Scrollbar,builtinThemeOverrides:this.inverted&&this.cssVars.__invertScrollbar===`true`?{colorHover:`rgba(255, 255, 255, .4)`,color:`rgba(255, 255, 255, .3)`}:void 0}),s(this.$slots),1040,[`onScroll`,`style`,`contentStyle`,`contentClass`,`theme`,`themeOverrides`,`builtinThemeOverrides`])),r?(y(),F(e,{key:2},[r===`bar`?(y(),C(xe,{key:0,clsPrefix:t,class:c(n?this.collapsedTriggerClass:this.triggerClass),style:h(n?this.collapsedTriggerStyle:this.triggerStyle),onClick:this.handleTriggerClick},null,8,[`clsPrefix`,`class`,`style`,`onClick`])):(y(),C(Ce,{key:1,clsPrefix:t,class:c(n?this.collapsedTriggerClass:this.triggerClass),style:h(n?this.collapsedTriggerStyle:this.triggerStyle),onClick:this.handleTriggerClick},null,8,[`clsPrefix`,`class`,`style`,`onClick`]))],64)):x(()=>null),this.bordered?(y(),F(`div`,{key:4,class:c(`${t}-layout-sider__border`)},null,2)):x(()=>null)],46,we)}}),J=P(`n-menu`),De=P(`n-submenu`),Y=P(`n-menu-item-group`),Oe=[m(`&::before`,`background-color: var(--n-item-color-hover);`),l(`arrow`,`
 color: var(--n-arrow-color-hover);
 `),l(`icon`,`
 color: var(--n-item-icon-color-hover);
 `),r(`menu-item-content-header`,`
 color: var(--n-item-text-color-hover);
 `,[m(`a`,`
 color: var(--n-item-text-color-hover);
 `),l(`extra`,`
 color: var(--n-item-text-color-hover);
 `)])],ke=[l(`icon`,`
 color: var(--n-item-icon-color-hover-horizontal);
 `),r(`menu-item-content-header`,`
 color: var(--n-item-text-color-hover-horizontal);
 `,[m(`a`,`
 color: var(--n-item-text-color-hover-horizontal);
 `),l(`extra`,`
 color: var(--n-item-text-color-hover-horizontal);
 `)])],Ae=m([r(`menu`,`
 background-color: var(--n-color);
 color: var(--n-item-text-color);
 overflow: hidden;
 transition: background-color .3s var(--n-bezier);
 box-sizing: border-box;
 font-size: var(--n-font-size);
 padding-bottom: 6px;
 `,[_(`horizontal`,`
 max-width: 100%;
 width: 100%;
 display: flex;
 overflow: hidden;
 padding-bottom: 0;
 `,[r(`submenu`,`margin: 0;`),r(`menu-item`,`margin: 0;`),r(`menu-item-content`,`
 padding: 0 20px;
 border-bottom: 2px solid #0000;
 `,[m(`&::before`,`display: none;`),_(`selected`,`border-bottom: 2px solid var(--n-border-color-horizontal)`)]),r(`menu-item-content`,[_(`selected`,[l(`icon`,`color: var(--n-item-icon-color-active-horizontal);`),r(`menu-item-content-header`,`
 color: var(--n-item-text-color-active-horizontal);
 `,[m(`a`,`color: var(--n-item-text-color-active-horizontal);`),l(`extra`,`color: var(--n-item-text-color-active-horizontal);`)])]),_(`child-active`,`
 border-bottom: 2px solid var(--n-border-color-horizontal);
 `,[r(`menu-item-content-header`,`
 color: var(--n-item-text-color-child-active-horizontal);
 `,[m(`a`,`
 color: var(--n-item-text-color-child-active-horizontal);
 `),l(`extra`,`
 color: var(--n-item-text-color-child-active-horizontal);
 `)]),l(`icon`,`
 color: var(--n-item-icon-color-child-active-horizontal);
 `)]),L(`disabled`,[L(`selected, child-active`,[m(`&:focus-within`,ke)]),_(`selected`,[X(null,[l(`icon`,`color: var(--n-item-icon-color-active-hover-horizontal);`),r(`menu-item-content-header`,`
 color: var(--n-item-text-color-active-hover-horizontal);
 `,[m(`a`,`color: var(--n-item-text-color-active-hover-horizontal);`),l(`extra`,`color: var(--n-item-text-color-active-hover-horizontal);`)])])]),_(`child-active`,[X(null,[l(`icon`,`color: var(--n-item-icon-color-child-active-hover-horizontal);`),r(`menu-item-content-header`,`
 color: var(--n-item-text-color-child-active-hover-horizontal);
 `,[m(`a`,`color: var(--n-item-text-color-child-active-hover-horizontal);`),l(`extra`,`color: var(--n-item-text-color-child-active-hover-horizontal);`)])])]),X(`border-bottom: 2px solid var(--n-border-color-horizontal);`,ke)]),r(`menu-item-content-header`,[m(`a`,`color: var(--n-item-text-color-horizontal);`)])])]),L(`responsive`,[r(`menu-item-content-header`,`
 overflow: hidden;
 text-overflow: ellipsis;
 `)]),_(`collapsed`,[r(`menu-item-content`,[_(`selected`,[m(`&::before`,`
 background-color: var(--n-item-color-active-collapsed) !important;
 `)]),r(`menu-item-content-header`,`opacity: 0;`),l(`arrow`,`opacity: 0;`),l(`icon`,`color: var(--n-item-icon-color-collapsed);`)])]),r(`menu-item`,`
 height: var(--n-item-height);
 margin-top: 6px;
 position: relative;
 `),r(`menu-item-content`,`
 box-sizing: border-box;
 line-height: 1.75;
 height: 100%;
 display: grid;
 grid-template-areas: "icon content arrow";
 grid-template-columns: auto 1fr auto;
 align-items: center;
 cursor: pointer;
 position: relative;
 padding-right: 18px;
 transition:
 background-color .3s var(--n-bezier),
 padding-left .3s var(--n-bezier),
 border-color .3s var(--n-bezier);
 `,[m(`> *`,`z-index: 1;`),m(`&::before`,`
 z-index: auto;
 content: "";
 background-color: #0000;
 position: absolute;
 left: 8px;
 right: 8px;
 top: 0;
 bottom: 0;
 pointer-events: none;
 border-radius: var(--n-border-radius);
 transition: background-color .3s var(--n-bezier);
 `),_(`disabled`,`
 opacity: .45;
 cursor: not-allowed;
 `),_(`collapsed`,[l(`arrow`,`transform: rotate(0);`)]),_(`selected`,[m(`&::before`,`background-color: var(--n-item-color-active);`),l(`arrow`,`color: var(--n-arrow-color-active);`),l(`icon`,`color: var(--n-item-icon-color-active);`),r(`menu-item-content-header`,`
 color: var(--n-item-text-color-active);
 `,[m(`a`,`color: var(--n-item-text-color-active);`),l(`extra`,`color: var(--n-item-text-color-active);`)])]),_(`child-active`,[r(`menu-item-content-header`,`
 color: var(--n-item-text-color-child-active);
 `,[m(`a`,`
 color: var(--n-item-text-color-child-active);
 `),l(`extra`,`
 color: var(--n-item-text-color-child-active);
 `)]),l(`arrow`,`
 color: var(--n-arrow-color-child-active);
 `),l(`icon`,`
 color: var(--n-item-icon-color-child-active);
 `)]),L(`disabled`,[L(`selected, child-active`,[m(`&:focus-within`,Oe)]),_(`selected`,[X(null,[l(`arrow`,`color: var(--n-arrow-color-active-hover);`),l(`icon`,`color: var(--n-item-icon-color-active-hover);`),r(`menu-item-content-header`,`
 color: var(--n-item-text-color-active-hover);
 `,[m(`a`,`color: var(--n-item-text-color-active-hover);`),l(`extra`,`color: var(--n-item-text-color-active-hover);`)])])]),_(`child-active`,[X(null,[l(`arrow`,`color: var(--n-arrow-color-child-active-hover);`),l(`icon`,`color: var(--n-item-icon-color-child-active-hover);`),r(`menu-item-content-header`,`
 color: var(--n-item-text-color-child-active-hover);
 `,[m(`a`,`color: var(--n-item-text-color-child-active-hover);`),l(`extra`,`color: var(--n-item-text-color-child-active-hover);`)])])]),_(`selected`,[X(null,[m(`&::before`,`background-color: var(--n-item-color-active-hover);`)])]),X(null,Oe)]),l(`icon`,`
 grid-area: icon;
 color: var(--n-item-icon-color);
 transition:
 color .3s var(--n-bezier),
 font-size .3s var(--n-bezier),
 margin-right .3s var(--n-bezier);
 box-sizing: content-box;
 display: inline-flex;
 align-items: center;
 justify-content: center;
 `),l(`arrow`,`
 grid-area: arrow;
 font-size: 16px;
 color: var(--n-arrow-color);
 transform: rotate(180deg);
 opacity: 1;
 transition:
 color .3s var(--n-bezier),
 transform 0.2s var(--n-bezier),
 opacity 0.2s var(--n-bezier);
 `),r(`menu-item-content-header`,`
 grid-area: content;
 transition:
 color .3s var(--n-bezier),
 opacity .3s var(--n-bezier);
 opacity: 1;
 white-space: nowrap;
 color: var(--n-item-text-color);
 `,[m(`a`,`
 outline: none;
 text-decoration: none;
 transition: color .3s var(--n-bezier);
 color: var(--n-item-text-color);
 `,[m(`&::before`,`
 content: "";
 position: absolute;
 left: 0;
 right: 0;
 top: 0;
 bottom: 0;
 `)]),l(`extra`,`
 font-size: .93em;
 color: var(--n-group-text-color);
 transition: color .3s var(--n-bezier);
 `)])]),r(`submenu`,`
 cursor: pointer;
 position: relative;
 margin-top: 6px;
 `,[r(`menu-item-content`,`
 height: var(--n-item-height);
 `),r(`submenu-children`,`
 overflow: hidden;
 padding: 0;
 `,[I({duration:`.2s`})])]),r(`menu-item-group`,[r(`menu-item-group-title`,`
 margin-top: 6px;
 color: var(--n-group-text-color);
 cursor: default;
 font-size: .93em;
 height: 36px;
 display: flex;
 align-items: center;
 transition:
 padding-left .3s var(--n-bezier),
 color .3s var(--n-bezier);
 `)])]),r(`menu-tooltip`,[m(`a`,`
 color: inherit;
 text-decoration: none;
 `)]),r(`menu-divider`,`
 transition: background-color .3s var(--n-bezier);
 background-color: var(--n-divider-color);
 height: 1px;
 margin: 6px 18px;
 `)]);function X(e,t){return[_(`hover`,e,t),m(`&:hover`,e,t)]}var je=T({name:`MenuDivider`,setup(){let{mergedClsPrefixRef:e,isHorizontalRef:t}=O(J);return()=>t.value?null:(y(),F(`div`,{key:1,class:c(`${e.value}-menu-divider`)},null,2))}}),Me=T({name:`ChevronDownFilled`,render(){return(()=>{let e=i(`f3af82a2aab086a5`);return e[0]||=k(`svg`,{viewBox:`0 0 16 16`,fill:`none`,xmlns:`http://www.w3.org/2000/svg`},[k(`path`,{d:`M3.20041 5.73966C3.48226 5.43613 3.95681 5.41856 4.26034 5.70041L8 9.22652L11.7397 5.70041C12.0432 5.41856 12.5177 5.43613 12.7996 5.73966C13.0815 6.0432 13.0639 6.51775 12.7603 6.7996L8.51034 10.7996C8.22258 11.0668 7.77743 11.0668 7.48967 10.7996L3.23966 6.7996C2.93613 6.51775 2.91856 6.0432 3.20041 5.73966Z`,fill:`currentColor`})],-1)})()}}),Ne=[`onClick`],Pe=T({name:`MenuOptionContent`,props:{collapsed:Boolean,disabled:Boolean,title:[String,Function],icon:Function,extra:[String,Function],showArrow:Boolean,childActive:Boolean,hover:Boolean,paddingLeft:Number,selected:Boolean,maxIconSize:{type:Number,required:!0},activeIconSize:{type:Number,required:!0},iconMarginRight:{type:Number,required:!0},clsPrefix:{type:String,required:!0},onClick:Function,tmNode:{type:Object,required:!0},isEllipsisPlaceholder:Boolean},setup(e){let{props:t}=O(J);return{menuProps:t,style:N(()=>{let{paddingLeft:t}=e;return{paddingLeft:t&&`${t}px`}}),iconStyle:N(()=>{let{maxIconSize:t,activeIconSize:n,iconMarginRight:r}=e;return{width:`${t}px`,height:`${t}px`,fontSize:`${n}px`,marginRight:`${r}px`}})}},render(){let{clsPrefix:t,tmNode:n,menuProps:{renderIcon:r,renderLabel:a,renderExtra:o,expandIcon:s}}=this,l=r?r(n.rawNode):D(this.icon);return(()=>{let r=i(`7bb10afc6caf8fa4`);return y(),F(`div`,{onClick:e=>{this.onClick?.(e)},role:`none`,class:c([`${t}-menu-item-content`,{[`${t}-menu-item-content--selected`]:this.selected,[`${t}-menu-item-content--collapsed`]:this.collapsed,[`${t}-menu-item-content--child-active`]:this.childActive,[`${t}-menu-item-content--disabled`]:this.disabled,[`${t}-menu-item-content--hover`]:this.hover}]),style:h(this.style)},[x(()=>l&&(y(),F(`div`,{class:c(`${t}-menu-item-content__icon`),style:h(this.iconStyle),role:`none`},[x(()=>[l])],6))),k(`div`,{class:c(`${t}-menu-item-content-header`),role:`none`},[this.isEllipsisPlaceholder?(y(),F(e,{key:0},[x(()=>this.title)],64)):(y(),F(e,{key:1},[a?(y(),F(e,{key:0},[x(()=>a(n.rawNode))],64)):(y(),F(e,{key:1},[x(()=>D(this.title))],64))],64)),this.extra||o?(y(),F(`span`,{key:2,class:c(`${t}-menu-item-content-header__extra`)},[r[0]||=x(` `,-1),o?(y(),F(e,{key:0},[x(()=>o(n.rawNode))],64)):(y(),F(e,{key:1},[x(()=>D(this.extra))],64))],2)):x(()=>null)],2),this.showArrow?(y(),C(u,{key:0,ariaHidden:!0,class:c(`${t}-menu-item-content__arrow`),clsPrefix:t},{default:()=>s?s(n.rawNode):(y(),C(Me,{key:1}))},1032,[`class`,`clsPrefix`])):x(()=>null)],14,Ne)})()}}),Fe=8;function Z(e){let t=O(J),{props:n,mergedCollapsedRef:r}=t,i=O(De,null),a=O(Y,null),o=N(()=>n.mode===`horizontal`),s=N(()=>o.value?n.dropdownPlacement:`tmNodes`in e?`right-start`:`right`),c=N(()=>Math.max(n.collapsedIconSize??n.iconSize,n.iconSize));return{dropdownPlacement:s,activeIconSize:N(()=>!o.value&&e.root&&r.value?n.collapsedIconSize??n.iconSize:n.iconSize),maxIconSize:c,paddingLeft:N(()=>{if(o.value)return;let{collapsedWidth:t,indent:s,rootIndent:l}=n,{root:u,isGroup:d}=e,f=l===void 0?s:l;return u?r.value?t/2-c.value/2:f:a&&typeof a.paddingLeftRef.value==`number`?r.value?t/2-c.value/2:s/2+a.paddingLeftRef.value:i&&typeof i.paddingLeftRef.value==`number`?(d?s/2:s)+i.paddingLeftRef.value:0}),iconMarginRight:N(()=>{let{collapsedWidth:t,indent:i,rootIndent:a}=n,{value:s}=c,{root:l}=e;return o.value||!l||!r.value?Fe:(a===void 0?i:a)+s+Fe-(t+s)/2}),NMenu:t,NSubmenu:i,NMenuOptionGroup:a}}var Q={internalKey:{type:[String,Number],required:!0},root:Boolean,isGroup:Boolean,level:{type:Number,required:!0},title:[String,Function],extra:[String,Function]},Ie={...Q,tmNode:{type:Object,required:!0},disabled:Boolean,icon:Function,onClick:Function},Le=V(Ie),Re=T({name:`MenuOption`,props:Ie,setup(e){let t=Z(e),{NSubmenu:n,NMenu:r,NMenuOptionGroup:i}=t,{props:a,mergedClsPrefixRef:o,mergedCollapsedRef:s}=r,c=n?n.mergedDisabledRef:i?i.mergedDisabledRef:{value:!1},l=N(()=>c.value||e.disabled);function u(t){let{onClick:n}=e;n&&n(t)}function d(t){l.value||(r.doSelect(e.internalKey,e.tmNode.rawNode),u(t))}return{mergedClsPrefix:o,dropdownPlacement:t.dropdownPlacement,paddingLeft:t.paddingLeft,iconMarginRight:t.iconMarginRight,maxIconSize:t.maxIconSize,activeIconSize:t.activeIconSize,mergedTheme:r.mergedThemeRef,menuProps:a,dropdownEnabled:A(()=>e.root&&s.value&&a.mode!==`horizontal`&&!l.value),selected:A(()=>r.mergedValueRef.value===e.internalKey),mergedDisabled:l,handleClick:d}},render(){let{mergedClsPrefix:e,mergedTheme:t,tmNode:n,menuProps:{renderLabel:r,nodeProps:i}}=this,a=i?.(n.rawNode);return y(),F(`div`,S(a,{role:`menuitem`,class:[`${e}-menu-item`,a?.class]}),[(y(),C(ae,{theme:t.peers.Tooltip,themeOverrides:t.peerOverrides.Tooltip,trigger:`hover`,placement:this.dropdownPlacement,disabled:!this.dropdownEnabled||this.title===void 0,internalExtraClass:[`menu-tooltip`]},{default:()=>r?r(n.rawNode):D(this.title),trigger:()=>(y(),C(Pe,{tmNode:n,clsPrefix:e,paddingLeft:this.paddingLeft,iconMarginRight:this.iconMarginRight,maxIconSize:this.maxIconSize,activeIconSize:this.activeIconSize,selected:this.selected,title:this.title,extra:this.extra,disabled:this.mergedDisabled,icon:this.icon,onClick:this.handleClick},null,8,[`tmNode`,`clsPrefix`,`paddingLeft`,`iconMarginRight`,`maxIconSize`,`activeIconSize`,`selected`,`title`,`extra`,`disabled`,`icon`,`onClick`]))},1032,[`theme`,`themeOverrides`,`placement`,`disabled`]))],16)}}),ze={...Q,tmNode:{type:Object,required:!0},tmNodes:{type:Array,required:!0}},Be=V(ze),Ve=T({name:`MenuOptionGroup`,props:ze,setup(n){let r=Z(n),{NSubmenu:a}=r,o=N(()=>a?.mergedDisabledRef.value?!0:n.tmNode.disabled);t(Y,{paddingLeftRef:r.paddingLeft,mergedDisabledRef:o});let{mergedClsPrefixRef:s,props:l}=O(J);return function(){let{value:t}=s,a=r.paddingLeft.value,{nodeProps:o}=l,u=o?.(n.tmNode.rawNode);return(()=>{let r=i(`45eca6a63be5028b`);return y(),F(`div`,{class:c(`${t}-menu-item-group`),role:`group`},[k(`div`,S(u,{class:[`${t}-menu-item-group-title`,u?.class],style:[u?.style||``,a===void 0?``:`padding-left: ${a}px;`]}),[x(()=>D(n.title)),n.extra?(y(),F(e,{key:0},[r[0]||=x(` `,-1),x(()=>D(n.extra))],64)):x(()=>null)],16),k(`div`,null,[x(()=>n.tmNodes.map(e=>Je(e,l)))])],2)})()}}}),He=[`aria-expanded`,`id`],Ue=[`aria-expanded`,`id`],We={...Q,rawNodes:{type:Array,default:()=>[]},tmNodes:{type:Array,default:()=>[]},tmNode:{type:Object,required:!0},disabled:Boolean,icon:Function,onClick:Function,domId:String,virtualChildActive:{type:Boolean,default:void 0},isEllipsisPlaceholder:Boolean},Ge=V(We),Ke=T({name:`Submenu`,props:We,setup(e){let n=Z(e),{NMenu:r,NSubmenu:i}=n,{props:a,mergedCollapsedRef:o,mergedThemeRef:s}=r,c=N(()=>{let{disabled:t}=e;return i?.mergedDisabledRef.value||a.disabled?!0:t}),l=b(!1);t(De,{paddingLeftRef:n.paddingLeft,mergedDisabledRef:c}),t(Y,null);function u(){let{onClick:t}=e;t&&t()}function d(){c.value||(o.value||r.toggleExpand(e.internalKey),u())}function f(e){l.value=e}return{menuProps:a,mergedTheme:s,doSelect:r.doSelect,inverted:r.invertedRef,isHorizontal:r.isHorizontalRef,mergedClsPrefix:r.mergedClsPrefixRef,maxIconSize:n.maxIconSize,activeIconSize:n.activeIconSize,iconMarginRight:n.iconMarginRight,dropdownPlacement:n.dropdownPlacement,dropdownShow:l,paddingLeft:n.paddingLeft,mergedDisabled:c,mergedValue:r.mergedValueRef,childActive:A(()=>e.virtualChildActive??r.activePathRef.value.includes(e.internalKey)),collapsed:N(()=>a.mode===`horizontal`?!1:o.value?!0:!r.mergedExpandedKeysRef.value.includes(e.internalKey)),dropdownEnabled:N(()=>!c.value&&(a.mode===`horizontal`||o.value)),handlePopoverShowChange:f,handleClick:d}},render(){let{mergedClsPrefix:t,menuProps:{renderIcon:n,renderLabel:r}}=this,i=()=>{let{isHorizontal:e,paddingLeft:t,collapsed:n,mergedDisabled:r,maxIconSize:i,activeIconSize:a,title:o,childActive:s,icon:c,handleClick:l,menuProps:{nodeProps:u},dropdownShow:d,iconMarginRight:f,tmNode:p,mergedClsPrefix:m,isEllipsisPlaceholder:h,extra:g}=this,_=u?.(p.rawNode);return y(),F(`div`,S(_,{class:[`${m}-menu-item`,_?.class],role:`menuitem`}),[(y(),C(Pe,{tmNode:p,paddingLeft:t,collapsed:n,disabled:r,iconMarginRight:f,maxIconSize:i,activeIconSize:a,title:o,extra:g,showArrow:!e,childActive:s,clsPrefix:m,icon:c,hover:d,onClick:l,isEllipsisPlaceholder:h},null,8,[`tmNode`,`paddingLeft`,`collapsed`,`disabled`,`iconMarginRight`,`maxIconSize`,`activeIconSize`,`title`,`extra`,`showArrow`,`childActive`,`clsPrefix`,`icon`,`hover`,`onClick`,`isEllipsisPlaceholder`]))],16)},a=()=>(y(),C(v,null,{default:()=>{let{tmNodes:e,collapsed:n}=this;return n?null:(y(),F(`div`,{key:1,class:c(`${t}-submenu-children`),role:`menu`},[x(()=>e.map(e=>Je(e,this.menuProps)))],2))}},1024));return this.root?(y(),C(oe,S({key:2,size:`large`,trigger:`hover`},this.menuProps?.dropdownProps,{themeOverrides:this.mergedTheme.peerOverrides.Dropdown,theme:this.mergedTheme.peers.Dropdown,builtinThemeOverrides:{fontSizeLarge:`14px`,optionIconSizeLarge:`18px`},value:this.mergedValue,disabled:!this.dropdownEnabled,placement:this.dropdownPlacement,keyField:this.menuProps.keyField,labelField:this.menuProps.labelField,childrenField:this.menuProps.childrenField,onUpdateShow:this.handlePopoverShowChange,options:this.rawNodes,onSelect:this.doSelect,inverted:this.inverted,renderIcon:n,renderLabel:r}),{default:()=>(y(),F(`div`,{class:c(`${t}-submenu`),role:`menu`,"aria-expanded":!this.collapsed,id:this.domId},[x(()=>i()),this.isHorizontal?x(()=>null):(y(),F(e,{key:1},[x(()=>a())],64))],10,He))},1040,[`themeOverrides`,`theme`,`value`,`disabled`,`placement`,`keyField`,`labelField`,`childrenField`,`onUpdateShow`,`options`,`onSelect`,`inverted`,`renderIcon`,`renderLabel`])):(y(),F(`div`,{key:3,class:c(`${t}-submenu`),role:`menu`,"aria-expanded":!this.collapsed,id:this.domId},[x(()=>i()),x(()=>a())],10,Ue))}});function $(e){return e.type===`divider`||e.type===`render`}function qe(e){return e.type===`divider`}function Je(e,t){let{rawNode:n}=e,{show:r}=n;if(r===!1)return null;if($(n))return qe(n)?(y(),C(je,S({key:e.key},n.props),null,16)):null;let{labelField:i}=t,{key:a,level:o,isGroup:s}=e,c={...n,title:n.title||n[i],extra:n.titleExtra||n.extra,key:a,internalKey:a,level:o,root:o===0,isGroup:s};return e.children?e.isGroup?j(Ve,B(c,Be,{tmNode:e,tmNodes:e.children,key:a})):j(Ke,B(c,Ge,{key:a,rawNodes:n[t.childrenField],tmNodes:e.children,tmNode:e})):j(Re,B(c,Le,{key:a,tmNode:e}))}var Ye={...p.props,options:{type:Array,default:()=>[]},collapsed:{type:Boolean,default:void 0},collapsedWidth:{type:Number,default:48},iconSize:{type:Number,default:20},collapsedIconSize:{type:Number,default:24},rootIndent:Number,indent:{type:Number,default:32},labelField:{type:String,default:`label`},keyField:{type:String,default:`key`},childrenField:{type:String,default:`children`},disabledField:{type:String,default:`disabled`},defaultExpandAll:Boolean,defaultExpandedKeys:Array,expandedKeys:Array,value:[String,Number],defaultValue:{type:[String,Number],default:null},mode:{type:String,default:`vertical`},watchProps:{type:Array,default:void 0},disabled:Boolean,show:{type:Boolean,default:!0},inverted:Boolean,"onUpdate:expandedKeys":[Function,Array],onUpdateExpandedKeys:[Function,Array],onUpdateValue:[Function,Array],"onUpdate:value":[Function,Array],expandIcon:Function,renderIcon:Function,renderLabel:Function,renderExtra:Function,dropdownProps:Object,accordion:Boolean,nodeProps:Function,dropdownPlacement:{type:String,default:`bottom`},responsive:Boolean,items:Array,onOpenNamesChange:[Function,Array],onSelect:[Function,Array],onExpandedNamesChange:[Function,Array],expandedNames:Array,defaultExpandedNames:Array},Xe=T({name:`Menu`,inheritAttrs:!1,props:Ye,setup(e){let{mergedClsPrefixRef:r,inlineThemeDisabled:i}=E(e),a=p(`Menu`,`-menu`,Ae,re,e,r),o=O(le,null),s=N(()=>{let{collapsed:t}=e;if(t!==void 0)return t;if(o){let{collapseModeRef:e,collapsedRef:t}=o;if(e.value===`width`)return t.value??!1}return!1}),c=N(()=>{let{keyField:t,childrenField:n,disabledField:r}=e;return U(e.items||e.options,{getIgnored(e){return $(e)},getChildren(e){return e[n]},getDisabled(e){return e[r]},getKey(e){return e[t]??e.name}})}),l=N(()=>new Set(c.value.treeNodes.map(e=>e.key))),{watchProps:u}=e,f=b(null);u?.includes(`defaultValue`)?d(()=>{f.value=e.defaultValue}):f.value=e.defaultValue;let m=M(e,`value`),h=W(m,f),g=b([]),_=()=>{g.value=e.defaultExpandAll?c.value.getNonLeafKeys():e.defaultExpandedNames||e.defaultExpandedKeys||c.value.getPath(h.value,{includeSelf:!1}).keyPath};u?.includes(`defaultExpandedKeys`)?d(_):_();let v=ie(e,[`expandedNames`,`expandedKeys`]),x=W(v,g),S=N(()=>c.value.treeNodes),w=N(()=>c.value.getPath(h.value).keyPath);t(J,{props:e,mergedCollapsedRef:s,mergedThemeRef:a,mergedValueRef:h,mergedExpandedKeysRef:x,activePathRef:w,mergedClsPrefixRef:r,isHorizontalRef:N(()=>e.mode===`horizontal`),invertedRef:M(e,`inverted`),doSelect:T,toggleExpand:k});function T(t,n){let{"onUpdate:value":r,onUpdateValue:i,onSelect:a}=e;i&&H(i,t,n),r&&H(r,t,n),a&&H(a,t,n),f.value=t}function D(t){let{"onUpdate:expandedKeys":n,onUpdateExpandedKeys:r,onExpandedNamesChange:i,onOpenNamesChange:a}=e;n&&H(n,t),r&&H(r,t),i&&H(i,t),a&&H(a,t),g.value=t}function k(t){let n=Array.from(x.value),r=n.findIndex(e=>e===t);if(~r)n.splice(r,1);else{if(e.accordion&&l.value.has(t)){let e=n.findIndex(e=>l.value.has(e));e>-1&&n.splice(e,1)}n.push(t)}D(n)}let A=t=>{let n=c.value.getPath(t??h.value,{includeSelf:!1}).keyPath;if(!n.length)return;let r=Array.from(x.value),i=new Set([...r,...n]);e.accordion&&l.value.forEach(e=>{i.has(e)&&!n.includes(e)&&i.delete(e)}),D(Array.from(i))},j=N(()=>{let{inverted:t}=e,{common:{cubicBezierEaseInOut:n},self:r}=a.value,{borderRadius:i,borderColorHorizontal:o,fontSize:s,itemHeight:c,dividerColor:l}=r,u={"--n-divider-color":l,"--n-bezier":n,"--n-font-size":s,"--n-border-color-horizontal":o,"--n-border-radius":i,"--n-item-height":c};return t?(u[`--n-group-text-color`]=r.groupTextColorInverted,u[`--n-color`]=r.colorInverted,u[`--n-item-text-color`]=r.itemTextColorInverted,u[`--n-item-text-color-hover`]=r.itemTextColorHoverInverted,u[`--n-item-text-color-active`]=r.itemTextColorActiveInverted,u[`--n-item-text-color-child-active`]=r.itemTextColorChildActiveInverted,u[`--n-item-text-color-child-active-hover`]=r.itemTextColorChildActiveInverted,u[`--n-item-text-color-active-hover`]=r.itemTextColorActiveHoverInverted,u[`--n-item-icon-color`]=r.itemIconColorInverted,u[`--n-item-icon-color-hover`]=r.itemIconColorHoverInverted,u[`--n-item-icon-color-active`]=r.itemIconColorActiveInverted,u[`--n-item-icon-color-active-hover`]=r.itemIconColorActiveHoverInverted,u[`--n-item-icon-color-child-active`]=r.itemIconColorChildActiveInverted,u[`--n-item-icon-color-child-active-hover`]=r.itemIconColorChildActiveHoverInverted,u[`--n-item-icon-color-collapsed`]=r.itemIconColorCollapsedInverted,u[`--n-item-text-color-horizontal`]=r.itemTextColorHorizontalInverted,u[`--n-item-text-color-hover-horizontal`]=r.itemTextColorHoverHorizontalInverted,u[`--n-item-text-color-active-horizontal`]=r.itemTextColorActiveHorizontalInverted,u[`--n-item-text-color-child-active-horizontal`]=r.itemTextColorChildActiveHorizontalInverted,u[`--n-item-text-color-child-active-hover-horizontal`]=r.itemTextColorChildActiveHoverHorizontalInverted,u[`--n-item-text-color-active-hover-horizontal`]=r.itemTextColorActiveHoverHorizontalInverted,u[`--n-item-icon-color-horizontal`]=r.itemIconColorHorizontalInverted,u[`--n-item-icon-color-hover-horizontal`]=r.itemIconColorHoverHorizontalInverted,u[`--n-item-icon-color-active-horizontal`]=r.itemIconColorActiveHorizontalInverted,u[`--n-item-icon-color-active-hover-horizontal`]=r.itemIconColorActiveHoverHorizontalInverted,u[`--n-item-icon-color-child-active-horizontal`]=r.itemIconColorChildActiveHorizontalInverted,u[`--n-item-icon-color-child-active-hover-horizontal`]=r.itemIconColorChildActiveHoverHorizontalInverted,u[`--n-arrow-color`]=r.arrowColorInverted,u[`--n-arrow-color-hover`]=r.arrowColorHoverInverted,u[`--n-arrow-color-active`]=r.arrowColorActiveInverted,u[`--n-arrow-color-active-hover`]=r.arrowColorActiveHoverInverted,u[`--n-arrow-color-child-active`]=r.arrowColorChildActiveInverted,u[`--n-arrow-color-child-active-hover`]=r.arrowColorChildActiveHoverInverted,u[`--n-item-color-hover`]=r.itemColorHoverInverted,u[`--n-item-color-active`]=r.itemColorActiveInverted,u[`--n-item-color-active-hover`]=r.itemColorActiveHoverInverted,u[`--n-item-color-active-collapsed`]=r.itemColorActiveCollapsedInverted):(u[`--n-group-text-color`]=r.groupTextColor,u[`--n-color`]=r.color,u[`--n-item-text-color`]=r.itemTextColor,u[`--n-item-text-color-hover`]=r.itemTextColorHover,u[`--n-item-text-color-active`]=r.itemTextColorActive,u[`--n-item-text-color-child-active`]=r.itemTextColorChildActive,u[`--n-item-text-color-child-active-hover`]=r.itemTextColorChildActiveHover,u[`--n-item-text-color-active-hover`]=r.itemTextColorActiveHover,u[`--n-item-icon-color`]=r.itemIconColor,u[`--n-item-icon-color-hover`]=r.itemIconColorHover,u[`--n-item-icon-color-active`]=r.itemIconColorActive,u[`--n-item-icon-color-active-hover`]=r.itemIconColorActiveHover,u[`--n-item-icon-color-child-active`]=r.itemIconColorChildActive,u[`--n-item-icon-color-child-active-hover`]=r.itemIconColorChildActiveHover,u[`--n-item-icon-color-collapsed`]=r.itemIconColorCollapsed,u[`--n-item-text-color-horizontal`]=r.itemTextColorHorizontal,u[`--n-item-text-color-hover-horizontal`]=r.itemTextColorHoverHorizontal,u[`--n-item-text-color-active-horizontal`]=r.itemTextColorActiveHorizontal,u[`--n-item-text-color-child-active-horizontal`]=r.itemTextColorChildActiveHorizontal,u[`--n-item-text-color-child-active-hover-horizontal`]=r.itemTextColorChildActiveHoverHorizontal,u[`--n-item-text-color-active-hover-horizontal`]=r.itemTextColorActiveHoverHorizontal,u[`--n-item-icon-color-horizontal`]=r.itemIconColorHorizontal,u[`--n-item-icon-color-hover-horizontal`]=r.itemIconColorHoverHorizontal,u[`--n-item-icon-color-active-horizontal`]=r.itemIconColorActiveHorizontal,u[`--n-item-icon-color-active-hover-horizontal`]=r.itemIconColorActiveHoverHorizontal,u[`--n-item-icon-color-child-active-horizontal`]=r.itemIconColorChildActiveHorizontal,u[`--n-item-icon-color-child-active-hover-horizontal`]=r.itemIconColorChildActiveHoverHorizontal,u[`--n-arrow-color`]=r.arrowColor,u[`--n-arrow-color-hover`]=r.arrowColorHover,u[`--n-arrow-color-active`]=r.arrowColorActive,u[`--n-arrow-color-active-hover`]=r.arrowColorActiveHover,u[`--n-arrow-color-child-active`]=r.arrowColorChildActive,u[`--n-arrow-color-child-active-hover`]=r.arrowColorChildActiveHover,u[`--n-item-color-hover`]=r.itemColorHover,u[`--n-item-color-active`]=r.itemColorActive,u[`--n-item-color-active-hover`]=r.itemColorActiveHover,u[`--n-item-color-active-collapsed`]=r.itemColorActiveCollapsed),u}),P=i?n(`menu`,N(()=>e.inverted?`a`:`b`),j,e):void 0,F=ee(),I=b(null),L=b(null),R=!0,z=()=>{R?R=!1:I.value?.sync({showAllItemsBeforeCalculate:!0})};function B(){return document.getElementById(F)}let V=b(-1);function te(t){V.value=e.options.length-t}function ne(e){e||(V.value=-1)}let ae=N(()=>{let t=V.value;return{children:t===-1?[]:e.options.slice(t)}}),oe=N(()=>{let{childrenField:t,disabledField:n,keyField:r}=e;return U([ae.value],{getIgnored(e){return $(e)},getChildren(e){return e[t]},getDisabled(e){return e[n]},getKey(e){return e[r]??e.name}})}),G=N(()=>U([{}]).treeNodes[0]);function se(){if(V.value===-1)return y(),C(Ke,{root:!0,level:0,key:`__ellpisisGroupPlaceholder__`,internalKey:`__ellpisisGroupPlaceholder__`,title:`···`,tmNode:G.value,domId:F,isEllipsisPlaceholder:!0},null,8,[`tmNode`,`domId`]);let e=oe.value.treeNodes[0],t=w.value,n=!!e.children?.some(e=>t.includes(e.key));return y(),C(Ke,{level:0,root:!0,key:`__ellpisisGroup__`,internalKey:`__ellpisisGroup__`,title:`···`,virtualChildActive:n,tmNode:e,domId:F,rawNodes:e.rawNode.children||[],tmNodes:e.children||[],isEllipsisPlaceholder:!0},null,8,[`virtualChildActive`,`tmNode`,`domId`,`rawNodes`,`tmNodes`])}return{mergedClsPrefix:r,controlledExpandedKeys:v,uncontrolledExpanededKeys:g,mergedExpandedKeys:x,uncontrolledValue:f,mergedValue:h,activePath:w,tmNodes:S,mergedTheme:a,mergedCollapsed:s,cssVars:i?void 0:j,themeClass:P?.themeClass,overflowRef:I,counterRef:L,updateCounter:()=>{},onResize:z,onUpdateOverflow:ne,onUpdateCount:te,renderCounter:se,getCounter:B,onRender:P?.onRender,showOption:A,deriveResponsiveState:z}},render(){let{mergedClsPrefix:e,mode:t,themeClass:n,onRender:r}=this;r?.();let i=()=>this.tmNodes.map(e=>Je(e,this.$props)),a=t===`horizontal`&&this.responsive,o=()=>j(`div`,S(this.$attrs,{role:t===`horizontal`?`menubar`:`menu`,class:[`${e}-menu`,n,`${e}-menu--${t}`,a&&`${e}-menu--responsive`,this.mergedCollapsed&&`${e}-menu--collapsed`],style:this.cssVars}),a?(y(),C(ne,{key:2,ref:`overflowRef`,onUpdateOverflow:this.onUpdateOverflow,getCounter:this.getCounter,onUpdateCount:this.onUpdateCount,updateCounter:this.updateCounter,style:{width:`100%`,display:`flex`,overflow:`hidden`}},{default:i,counter:this.renderCounter},1032,[`onUpdateOverflow`,`getCounter`,`onUpdateCount`,`updateCounter`])):i());return a?(y(),C(te,{key:3,onResize:this.onResize},{default:o},1032,[`onResize`])):o()}}),Ze={xmlns:`http://www.w3.org/2000/svg`,"xmlns:xlink":`http://www.w3.org/1999/xlink`,viewBox:`0 0 512 512`},Qe=T({name:`BookOutline`,render:function(e,t){return y(),F(`svg`,Ze,t[0]||=[k(`path`,{d:`M256 160c16-63.16 76.43-95.41 208-96a15.94 15.94 0 0 1 16 16v288a16 16 0 0 1-16 16c-128 0-177.45 25.81-208 64c-30.37-38-80-64-208-64c-9.88 0-16-8.05-16-17.93V80a15.94 15.94 0 0 1 16-16c131.57.59 192 32.84 208 96z`,fill:`none`,stroke:`currentColor`,"stroke-linecap":`round`,"stroke-linejoin":`round`,"stroke-width":`32`},null,-1),k(`path`,{fill:`none`,stroke:`currentColor`,"stroke-linecap":`round`,"stroke-linejoin":`round`,"stroke-width":`32`,d:`M256 160v288`},null,-1)])}}),$e={xmlns:`http://www.w3.org/2000/svg`,"xmlns:xlink":`http://www.w3.org/1999/xlink`,viewBox:`0 0 512 512`},et=T({name:`ClipboardOutline`,render:function(e,t){return y(),F(`svg`,$e,t[0]||=[k(`path`,{d:`M336 64h32a48 48 0 0 1 48 48v320a48 48 0 0 1-48 48H144a48 48 0 0 1-48-48V112a48 48 0 0 1 48-48h32`,fill:`none`,stroke:`currentColor`,"stroke-linejoin":`round`,"stroke-width":`32`},null,-1),k(`rect`,{x:`176`,y:`32`,width:`160`,height:`64`,rx:`26.13`,ry:`26.13`,fill:`none`,stroke:`currentColor`,"stroke-linejoin":`round`,"stroke-width":`32`},null,-1)])}}),tt={xmlns:`http://www.w3.org/2000/svg`,"xmlns:xlink":`http://www.w3.org/1999/xlink`,viewBox:`0 0 512 512`},nt=T({name:`CreateOutline`,render:function(e,t){return y(),F(`svg`,tt,t[0]||=[k(`path`,{d:`M384 224v184a40 40 0 0 1-40 40H104a40 40 0 0 1-40-40V168a40 40 0 0 1 40-40h167.48`,fill:`none`,stroke:`currentColor`,"stroke-linecap":`round`,"stroke-linejoin":`round`,"stroke-width":`32`},null,-1),k(`path`,{d:`M459.94 53.25a16.06 16.06 0 0 0-23.22-.56L424.35 65a8 8 0 0 0 0 11.31l11.34 11.32a8 8 0 0 0 11.34 0l12.06-12c6.1-6.09 6.67-16.01.85-22.38z`,fill:`currentColor`},null,-1),k(`path`,{d:`M399.34 90L218.82 270.2a9 9 0 0 0-2.31 3.93L208.16 299a3.91 3.91 0 0 0 4.86 4.86l24.85-8.35a9 9 0 0 0 3.93-2.31L422 112.66a9 9 0 0 0 0-12.66l-9.95-10a9 9 0 0 0-12.71 0z`,fill:`currentColor`},null,-1)])}}),rt={xmlns:`http://www.w3.org/2000/svg`,"xmlns:xlink":`http://www.w3.org/1999/xlink`,viewBox:`0 0 512 512`},it=T({name:`DocumentTextOutline`,render:function(e,t){return y(),F(`svg`,rt,t[0]||=[k(`path`,{d:`M416 221.25V416a48 48 0 0 1-48 48H144a48 48 0 0 1-48-48V96a48 48 0 0 1 48-48h98.75a32 32 0 0 1 22.62 9.37l141.26 141.26a32 32 0 0 1 9.37 22.62z`,fill:`none`,stroke:`currentColor`,"stroke-linejoin":`round`,"stroke-width":`32`},null,-1),k(`path`,{d:`M256 56v120a32 32 0 0 0 32 32h120`,fill:`none`,stroke:`currentColor`,"stroke-linecap":`round`,"stroke-linejoin":`round`,"stroke-width":`32`},null,-1),k(`path`,{fill:`none`,stroke:`currentColor`,"stroke-linecap":`round`,"stroke-linejoin":`round`,"stroke-width":`32`,d:`M176 288h160`},null,-1),k(`path`,{fill:`none`,stroke:`currentColor`,"stroke-linecap":`round`,"stroke-linejoin":`round`,"stroke-width":`32`,d:`M176 368h160`},null,-1)])}}),at={xmlns:`http://www.w3.org/2000/svg`,"xmlns:xlink":`http://www.w3.org/1999/xlink`,viewBox:`0 0 512 512`},ot=T({name:`ExtensionPuzzleOutline`,render:function(e,t){return y(),F(`svg`,at,t[0]||=[k(`path`,{fill:`none`,stroke:`currentColor`,"stroke-linecap":`round`,"stroke-linejoin":`round`,"stroke-width":`32`,d:`M413.66 246.1H386a2 2 0 0 1-2-2v-77.24A38.86 38.86 0 0 0 345.14 128H267.9a2 2 0 0 1-2-2V98.34c0-27.14-21.5-49.86-48.64-50.33a49.53 49.53 0 0 0-50.4 49.51V126a2 2 0 0 1-2 2H87.62A39.74 39.74 0 0 0 48 167.62V238a2 2 0 0 0 2 2h26.91c29.37 0 53.68 25.48 54.09 54.85c.42 29.87-23.51 57.15-53.29 57.15H50a2 2 0 0 0-2 2v70.38A39.74 39.74 0 0 0 87.62 464H158a2 2 0 0 0 2-2v-20.93c0-30.28 24.75-56.35 55-57.06c30.1-.7 57 20.31 57 50.28V462a2 2 0 0 0 2 2h71.14A38.86 38.86 0 0 0 384 425.14v-78a2 2 0 0 1 2-2h28.48c27.63 0 49.52-22.67 49.52-50.4s-23.2-48.64-50.34-48.64z`},null,-1)])}}),st={xmlns:`http://www.w3.org/2000/svg`,"xmlns:xlink":`http://www.w3.org/1999/xlink`,viewBox:`0 0 512 512`},ct=T({name:`HomeOutline`,render:function(e,t){return y(),F(`svg`,st,t[0]||=[k(`path`,{d:`M80 212v236a16 16 0 0 0 16 16h96V328a24 24 0 0 1 24-24h80a24 24 0 0 1 24 24v136h96a16 16 0 0 0 16-16V212`,fill:`none`,stroke:`currentColor`,"stroke-linecap":`round`,"stroke-linejoin":`round`,"stroke-width":`32`},null,-1),k(`path`,{d:`M480 256L266.89 52c-5-5.28-16.69-5.34-21.78 0L32 256`,fill:`none`,stroke:`currentColor`,"stroke-linecap":`round`,"stroke-linejoin":`round`,"stroke-width":`32`},null,-1),k(`path`,{fill:`none`,stroke:`currentColor`,"stroke-linecap":`round`,"stroke-linejoin":`round`,"stroke-width":`32`,d:`M400 179V64h-48v69`},null,-1)])}}),lt={xmlns:`http://www.w3.org/2000/svg`,"xmlns:xlink":`http://www.w3.org/1999/xlink`,viewBox:`0 0 512 512`},ut=T({name:`PencilOutline`,render:function(e,t){return y(),F(`svg`,lt,t[0]||=[k(`path`,{fill:`none`,stroke:`currentColor`,"stroke-linecap":`round`,"stroke-linejoin":`round`,"stroke-width":`32`,d:`M364.13 125.25L87 403l-23 45l44.99-23l277.76-277.13l-22.62-22.62z`},null,-1),k(`path`,{d:`M420.69 68.69l-22.62 22.62l22.62 22.63l22.62-22.63a16 16 0 0 0 0-22.62h0a16 16 0 0 0-22.62 0z`,fill:`none`,stroke:`currentColor`,"stroke-linecap":`round`,"stroke-linejoin":`round`,"stroke-width":`32`},null,-1)])}}),dt={xmlns:`http://www.w3.org/2000/svg`,"xmlns:xlink":`http://www.w3.org/1999/xlink`,viewBox:`0 0 512 512`},ft=T({name:`PersonOutline`,render:function(e,t){return y(),F(`svg`,dt,t[0]||=[k(`path`,{d:`M344 144c-3.92 52.87-44 96-88 96s-84.15-43.12-88-96c-4-55 35-96 88-96s92 42 88 96z`,fill:`none`,stroke:`currentColor`,"stroke-linecap":`round`,"stroke-linejoin":`round`,"stroke-width":`32`},null,-1),k(`path`,{d:`M256 304c-87 0-175.3 48-191.64 138.6C62.39 453.52 68.57 464 80 464h352c11.44 0 17.62-10.48 15.65-21.4C431.3 352 343 304 256 304z`,fill:`none`,stroke:`currentColor`,"stroke-miterlimit":`10`,"stroke-width":`32`},null,-1)])}}),pt={xmlns:`http://www.w3.org/2000/svg`,"xmlns:xlink":`http://www.w3.org/1999/xlink`,viewBox:`0 0 512 512`},mt=T({name:`ReaderOutline`,render:function(e,t){return y(),F(`svg`,pt,t[0]||=[k(`rect`,{x:`96`,y:`48`,width:`320`,height:`416`,rx:`48`,ry:`48`,fill:`none`,stroke:`currentColor`,"stroke-linejoin":`round`,"stroke-width":`32`},null,-1),k(`path`,{fill:`none`,stroke:`currentColor`,"stroke-linecap":`round`,"stroke-linejoin":`round`,"stroke-width":`32`,d:`M176 128h160`},null,-1),k(`path`,{fill:`none`,stroke:`currentColor`,"stroke-linecap":`round`,"stroke-linejoin":`round`,"stroke-width":`32`,d:`M176 208h160`},null,-1),k(`path`,{fill:`none`,stroke:`currentColor`,"stroke-linecap":`round`,"stroke-linejoin":`round`,"stroke-width":`32`,d:`M176 288h80`},null,-1)])}}),ht={xmlns:`http://www.w3.org/2000/svg`,"xmlns:xlink":`http://www.w3.org/1999/xlink`,viewBox:`0 0 512 512`},gt=T({name:`SchoolOutline`,render:function(e,t){return y(),F(`svg`,ht,t[0]||=[k(`path`,{fill:`none`,stroke:`currentColor`,"stroke-linecap":`round`,"stroke-linejoin":`round`,"stroke-width":`32`,d:`M32 192L256 64l224 128l-224 128L32 192z`},null,-1),k(`path`,{fill:`none`,stroke:`currentColor`,"stroke-linecap":`round`,"stroke-linejoin":`round`,"stroke-width":`32`,d:`M112 240v128l144 80l144-80V240`},null,-1),k(`path`,{fill:`none`,stroke:`currentColor`,"stroke-linecap":`round`,"stroke-linejoin":`round`,"stroke-width":`32`,d:`M480 368V192`},null,-1),k(`path`,{fill:`none`,stroke:`currentColor`,"stroke-linecap":`round`,"stroke-linejoin":`round`,"stroke-width":`32`,d:`M256 320v128`},null,-1)])}}),_t={xmlns:`http://www.w3.org/2000/svg`,"xmlns:xlink":`http://www.w3.org/1999/xlink`,viewBox:`0 0 512 512`},vt=T({name:`SettingsOutline`,render:function(e,t){return y(),F(`svg`,_t,t[0]||=[k(`path`,{d:`M262.29 192.31a64 64 0 1 0 57.4 57.4a64.13 64.13 0 0 0-57.4-57.4zM416.39 256a154.34 154.34 0 0 1-1.53 20.79l45.21 35.46a10.81 10.81 0 0 1 2.45 13.75l-42.77 74a10.81 10.81 0 0 1-13.14 4.59l-44.9-18.08a16.11 16.11 0 0 0-15.17 1.75A164.48 164.48 0 0 1 325 400.8a15.94 15.94 0 0 0-8.82 12.14l-6.73 47.89a11.08 11.08 0 0 1-10.68 9.17h-85.54a11.11 11.11 0 0 1-10.69-8.87l-6.72-47.82a16.07 16.07 0 0 0-9-12.22a155.3 155.3 0 0 1-21.46-12.57a16 16 0 0 0-15.11-1.71l-44.89 18.07a10.81 10.81 0 0 1-13.14-4.58l-42.77-74a10.8 10.8 0 0 1 2.45-13.75l38.21-30a16.05 16.05 0 0 0 6-14.08c-.36-4.17-.58-8.33-.58-12.5s.21-8.27.58-12.35a16 16 0 0 0-6.07-13.94l-38.19-30A10.81 10.81 0 0 1 49.48 186l42.77-74a10.81 10.81 0 0 1 13.14-4.59l44.9 18.08a16.11 16.11 0 0 0 15.17-1.75A164.48 164.48 0 0 1 187 111.2a15.94 15.94 0 0 0 8.82-12.14l6.73-47.89A11.08 11.08 0 0 1 213.23 42h85.54a11.11 11.11 0 0 1 10.69 8.87l6.72 47.82a16.07 16.07 0 0 0 9 12.22a155.3 155.3 0 0 1 21.46 12.57a16 16 0 0 0 15.11 1.71l44.89-18.07a10.81 10.81 0 0 1 13.14 4.58l42.77 74a10.8 10.8 0 0 1-2.45 13.75l-38.21 30a16.05 16.05 0 0 0-6.05 14.08c.33 4.14.55 8.3.55 12.47z`,fill:`none`,stroke:`currentColor`,"stroke-linecap":`round`,"stroke-linejoin":`round`,"stroke-width":`32`},null,-1)])}}),yt={xmlns:`http://www.w3.org/2000/svg`,"xmlns:xlink":`http://www.w3.org/1999/xlink`,viewBox:`0 0 512 512`},bt={HomeOutline:ct,CreateOutline:nt,ClipboardOutline:et,DocumentTextOutline:it,ExtensionPuzzleOutline:ot,ReaderOutline:mt,PencilOutline:ut,TrophyOutline:T({name:`TrophyOutline`,render:function(e,t){return y(),F(`svg`,yt,t[0]||=[w(`<path fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="32" d="M176 464h160"></path><path fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="32" d="M256 464V336"></path><path d="M384 224c0-50.64-.08-134.63-.12-160a16 16 0 0 0-16-16l-223.79.26a16 16 0 0 0-16 15.95c0 30.58-.13 129.17-.13 159.79c0 64.28 83 112 128 112S384 288.28 384 224z" fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="32"></path><path d="M128 96H48v16c0 55.22 33.55 112 80 112" fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="32"></path><path d="M384 96h80v16c0 55.22-33.55 112-80 112" fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="32"></path>`,5)])}}),BookOutline:Qe,SchoolOutline:gt,PersonOutline:ft,SettingsOutline:vt},xt=[{key:`practice`,label:`刷题`,icon:`CreateOutline`,children:[{key:`/practice/reading`,label:`阅读理解`,icon:`DocumentTextOutline`},{key:`/practice/matching`,label:`五选五`,icon:`ExtensionPuzzleOutline`},{key:`/practice/cloze`,label:`完形填空`,icon:`ReaderOutline`},{key:`/practice/grammar-fill`,label:`语法填空`,icon:`CreateOutline`},{key:`/practice/writing`,label:`作文`,icon:`PencilOutline`},{key:`/exam`,label:`真题模考`,icon:`TrophyOutline`}]},{key:`learn`,label:`学习`,icon:`BookOutline`,children:[{key:`/learn/vocab`,label:`背单词`,icon:`BookOutline`},{key:`/learn/grammar`,label:`学语法`,icon:`SchoolOutline`}]},{key:`user`,label:`我的`,icon:`PersonOutline`,children:[{key:`/user/stats`,label:`学习统计`},{key:`/user/submissions`,label:`我的提交`},{key:`/user/books`,label:`我的词书`},{key:`/user/mistakes`,label:`错题本`},{key:`/user/favorites`,label:`收藏`},{key:`/user/settings`,label:`设置`}]}],St={key:`admin`,label:`管理`,icon:`SettingsOutline`,children:[{key:`/admin/overview`,label:`运营概览`,icon:`HomeOutline`},{key:`/admin/questions`,label:`题库管理`,icon:`CreateOutline`},{key:`/admin/submissions`,label:`批改工作台`,icon:`ClipboardOutline`},{key:`/admin/users`,label:`用户管理`,icon:`PersonOutline`},{key:`/admin/vocab`,label:`词书与单词`,icon:`BookOutline`},{key:`/admin/grammar`,label:`语法管理`,icon:`ReaderOutline`},{key:`/admin/exam-papers`,label:`真题卷管理`,icon:`DocumentTextOutline`}]},Ct=[{key:`home`,label:`首页`,path:`/dashboard`,icon:`HomeOutline`},{key:`practice`,label:`刷题`,path:`/practice/reading`,icon:`CreateOutline`},{key:`learn`,label:`学习`,path:`/learn/vocab`,icon:`BookOutline`},{key:`user`,label:`我的`,path:`/user/stats`,icon:`PersonOutline`}];function wt(e){return[...xt,St].find(t=>t.key===e)?.children??[]}export{bt as a,ve as c,q as d,K as f,xt as i,he as l,wt as n,Xe as o,Ct as r,Ee as s,St as t,me as u};