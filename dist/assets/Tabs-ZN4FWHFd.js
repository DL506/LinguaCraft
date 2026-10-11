import{$t as e,Bt as t,Cn as n,D as r,En as i,F as a,Ft as o,J as s,K as c,Lt as l,M as u,Mn as d,Mt as f,Ot as p,P as m,Pn as h,Pt as g,Qn as _,Rt as v,Tn as y,Tt as b,Un as x,Y as S,Yt as C,Z as w,_n as T,an as E,dn as D,et as O,g as k,hn as A,in as j,it as M,jn as N,k as P,nn as F,pn as I,qn as L,qt as ee,rn as R,rt as te,sn as z,u as ne,vn as B,zt as V}from"./light-TBGKFU-e.js";import{H,K as re,at as ie,h as ae,k as U,q as W}from"./request-BS4zoC18.js";import{n as oe,r as se,t as ce}from"./cssr-BSqWL002.js";import{t as le}from"./use-merged-state-Cqr2carY.js";import{t as ue}from"./use-compitable-CpPvjT_b.js";import{t as de}from"./ChevronLeft-B6hCM0nL.js";import{t as fe}from"./ChevronRight-CqswQINz.js";import{t as pe}from"./Add-BKA48-oe.js";import{n as me}from"./light-CLshLnPZ.js";var he=/\s/;function ge(e){for(var t=e.length;t--&&he.test(e.charAt(t)););return t}var _e=/^\s+/;function ve(e){return e&&e.slice(0,ge(e)+1).replace(_e,``)}var G=NaN,K=/^[-+]0x[0-9a-f]+$/i,q=/^0b[01]+$/i,J=/^0o[0-7]+$/i,ye=parseInt;function be(e){if(typeof e==`number`)return e;if(p(e))return G;if(b(e)){var t=typeof e.valueOf==`function`?e.valueOf():e;e=b(t)?t+``:t}if(typeof e!=`string`)return e===0?e:+e;e=ve(e);var n=q.test(e);return n||J.test(e)?ye(e.slice(2),n?2:8):K.test(e)?G:+e}var Y=function(){return f.Date.now()},xe=`Expected a function`,X=Math.max,Se=Math.min;function Ce(e,t,n){var r,i,a,o,s,c,l=0,u=!1,d=!1,f=!0;if(typeof e!=`function`)throw TypeError(xe);t=be(t)||0,b(n)&&(u=!!n.leading,d=`maxWait`in n,a=d?X(be(n.maxWait)||0,t):a,f=`trailing`in n?!!n.trailing:f);function p(t){var n=r,a=i;return r=i=void 0,l=t,o=e.apply(a,n),o}function m(e){return l=e,s=setTimeout(_,t),u?p(e):o}function h(e){var n=e-c,r=e-l,i=t-n;return d?Se(i,a-r):i}function g(e){var n=e-c,r=e-l;return c===void 0||n>=t||n<0||d&&r>=a}function _(){var e=Y();if(g(e))return v(e);s=setTimeout(_,h(e))}function v(e){return s=void 0,f&&r?p(e):(r=i=void 0,o)}function y(){s!==void 0&&clearTimeout(s),l=0,r=c=i=s=void 0}function x(){return s===void 0?o:v(Y())}function S(){var e=Y(),n=g(e);if(r=arguments,i=this,c=e,n){if(s===void 0)return m(c);if(d)return clearTimeout(s),s=setTimeout(_,t),p(c)}return s===void 0&&(s=setTimeout(_,t)),o}return S.cancel=y,S.flush=x,S}var we=`Expected a function`;function Z(e,t,n){var r=!0,i=!0;if(typeof e!=`function`)throw TypeError(we);return b(n)&&(r=`leading`in n?!!n.leading:r,i=`trailing`in n?!!n.trailing:i),Ce(e,t,{leading:r,maxWait:t,trailing:i})}var Te=ce(`.v-x-scroll`,{overflow:`auto`,scrollbarWidth:`none`},[ce(`&::-webkit-scrollbar`,{width:0,height:0})]),Q=D({name:`XScroll`,props:{disabled:Boolean,onScroll:Function},setup(){let e=x(null);function t(e){e.currentTarget.offsetWidth<e.currentTarget.scrollWidth&&e.deltaY!==0&&(e.currentTarget.scrollLeft+=e.deltaY+e.deltaX,e.preventDefault())}let n=w();return Te.mount({id:`vueuc/x-scroll`,head:!0,anchorMetaName:oe,ssr:n}),Object.assign({selfRef:e,handleWheel:t},{scrollTo(...t){var n;(n=e.value)==null||n.scrollTo(...t)}})},render(){return I(`div`,{ref:`selfRef`,onScroll:this.onScroll,onWheel:this.disabled?void 0:this.handleWheel,class:`v-x-scroll`},this.$slots)}}),Ee=te(`n-tabs`),De={tab:[String,Number,Object,Function],name:{type:[String,Number],required:!0},disabled:Boolean,displayDirective:{type:String,default:`if`},closable:{type:Boolean,default:void 0},tabProps:Object,label:[String,Number,Object,Function]},Oe=D({__TAB_PANE__:!0,name:`TabPane`,alias:[`TabPanel`],props:De,slots:Object,setup(e){let t=A(Ee,null);return t||M(`tab-pane`,"`n-tab-pane` must be placed inside `n-tabs`."),{style:t.paneStyleRef,class:t.paneClassRef,mergedClsPrefix:t.mergedClsPrefixRef}},render(){return y(),z(`div`,{class:c([`${this.mergedClsPrefix}-tab-pane`,this.class]),style:_(this.style)},[S(()=>this.$slots.default?.())],6)}}),ke=[`data-name`,`data-disabled`],Ae={internalLeftPadded:Boolean,internalAddable:Boolean,internalCreatedByPane:Boolean,...ne(De,[`displayDirective`])},$=D({__TAB__:!0,inheritAttrs:!1,name:`Tab`,props:Ae,setup(e){let{mergedClsPrefixRef:t,valueRef:n,typeRef:r,closableRef:i,tabStyleRef:a,addTabStyleRef:o,tabClassRef:s,addTabClassRef:c,tabChangeIdRef:l,onBeforeLeaveRef:u,triggerRef:d,handleAdd:f,activateTab:p,handleClose:m}=A(Ee);return{trigger:d,mergedClosable:R(()=>{if(e.internalAddable)return!1;let{closable:t}=e;return t===void 0?i.value:t}),style:a,addStyle:o,tabClass:s,addTabClass:c,clsPrefix:t,value:n,type:r,handleClose(t){t.stopPropagation(),!e.disabled&&m(e.name)},activateTab(){if(e.disabled)return;if(e.internalAddable){f();return}let{name:t}=e,r=++l.id;if(t!==n.value){let{value:i}=u;i?Promise.resolve(i(e.name,n.value)).then(e=>{e&&l.id===r&&p(t)}):p(t)}}}},render(){let{internalAddable:t,clsPrefix:n,name:i,disabled:a,label:o,tab:s,value:l,mergedClosable:d,trigger:f,$slots:{default:p}}=this,m=o??s;return y(),z(`div`,{class:c(`${n}-tabs-tab-wrapper`)},[this.internalLeftPadded?(y(),z(`div`,{key:0,class:c(`${n}-tabs-tab-pad`)},null,2)):S(()=>null),(y(),z(`div`,T({key:i,"data-name":i,"data-disabled":a?!0:void 0},T({class:[`${n}-tabs-tab`,l===i&&`${n}-tabs-tab--active`,a&&`${n}-tabs-tab--disabled`,d&&`${n}-tabs-tab--closable`,t&&`${n}-tabs-tab--addable`,t?this.addTabClass:this.tabClass],onClick:f===`click`?this.activateTab:void 0,onMouseenter:f===`hover`?this.activateTab:void 0,style:t?this.addStyle:this.style},this.internalCreatedByPane?this.tabProps||{}:this.$attrs)),[j(`span`,{class:c(`${n}-tabs-tab__label`)},[t?(y(),z(e,{key:0},[j(`div`,{class:c(`${n}-tabs-tab__height-placeholder`)},`\xA0`,2),(y(),E(u,{clsPrefix:n},{default:()=>(y(),E(pe))},1032,[`clsPrefix`]))],64)):(y(),z(e,{key:1},[p?(y(),z(e,{key:0},[S(()=>p())],64)):(y(),z(e,{key:1},[typeof m==`object`?(y(),z(e,{key:0},[S(()=>m)],64)):(y(),z(e,{key:1},[S(()=>k(m??i))],64))],64))],64))],2),d&&this.type===`card`?(y(),E(r,{key:0,clsPrefix:n,class:c(`${n}-tabs-tab__close`),onClick:this.handleClose,disabled:a},null,8,[`clsPrefix`,`class`,`onClick`,`disabled`])):S(()=>null)],16,ke))],2)}}),je=o(`tabs`,`
 box-sizing: border-box;
 width: 100%;
 display: flex;
 flex-direction: column;
 transition:
 background-color .3s var(--n-bezier),
 border-color .3s var(--n-bezier);
`,[g(`&.transition-disabled`,[o(`tabs-tab`,`
 transition: none !important;
 `),o(`tabs-nav-scroll-content`,`
 transition: none !important;
 `),o(`tabs-tab-pad`,`
 transition: none !important;
 `)]),v(`segment-type`,[o(`tabs-rail`,[g(`&.transition-disabled`,[o(`tabs-capsule`,`
 transition: none;
 `)])])]),v(`top`,[o(`tab-pane`,`
 padding: var(--n-pane-padding-top) var(--n-pane-padding-right) var(--n-pane-padding-bottom) var(--n-pane-padding-left);
 `)]),v(`left`,[o(`tab-pane`,`
 padding: var(--n-pane-padding-right) var(--n-pane-padding-bottom) var(--n-pane-padding-left) var(--n-pane-padding-top);
 `)]),v(`left, right`,`
 flex-direction: row;
 `,[o(`tabs-bar`,`
 width: 2px;
 right: 0;
 transition:
 top .2s var(--n-bezier),
 max-height .2s var(--n-bezier),
 background-color .3s var(--n-bezier);
 `),o(`tabs-tab`,`
 padding: var(--n-tab-padding-vertical); 
 `)]),v(`right`,`
 flex-direction: row-reverse;
 `,[o(`tab-pane`,`
 padding: var(--n-pane-padding-left) var(--n-pane-padding-top) var(--n-pane-padding-right) var(--n-pane-padding-bottom);
 `),o(`tabs-bar`,`
 left: 0;
 `)]),v(`bottom`,`
 flex-direction: column-reverse;
 justify-content: flex-end;
 `,[o(`tab-pane`,`
 padding: var(--n-pane-padding-bottom) var(--n-pane-padding-right) var(--n-pane-padding-top) var(--n-pane-padding-left);
 `),o(`tabs-bar`,`
 top: 0;
 `)]),o(`tabs-rail`,`
 position: relative;
 padding: 3px;
 border-radius: var(--n-tab-border-radius);
 width: 100%;
 background-color: var(--n-color-segment);
 transition: background-color .3s var(--n-bezier);
 display: flex;
 align-items: center;
 `,[o(`tabs-capsule`,`
 border-radius: var(--n-tab-border-radius);
 position: absolute;
 left: 0;
 top: 0;
 pointer-events: none;
 background-color: var(--n-tab-color-segment);
 box-shadow: 0 1px 3px 0 rgba(0, 0, 0, .08);
 transition: transform 0.3s var(--n-bezier);
 `),o(`tabs-tab-wrapper`,`
 flex-basis: 0;
 flex-grow: 1;
 display: flex;
 align-items: center;
 justify-content: center;
 `,[o(`tabs-tab`,`
 overflow: hidden;
 border-radius: var(--n-tab-border-radius);
 width: 100%;
 display: flex;
 align-items: center;
 justify-content: center;
 `,[v(`active`,`
 font-weight: var(--n-font-weight-strong);
 color: var(--n-tab-text-color-active);
 `),g(`&:hover`,`
 color: var(--n-tab-text-color-hover);
 `)])])]),v(`flex`,[o(`tabs-nav`,`
 width: 100%;
 position: relative;
 `,[o(`tabs-wrapper`,`
 width: 100%;
 `,[o(`tabs-tab`,`
 margin-right: 0;
 `)])])]),o(`tabs-nav`,`
 box-sizing: border-box;
 line-height: 1.5;
 display: flex;
 transition: border-color .3s var(--n-bezier);
 `,[l(`prefix, suffix`,`
 display: flex;
 align-items: center;
 `),l(`prefix`,`padding-right: 16px;`),l(`suffix`,`padding-left: 16px;`)]),v(`top, bottom`,[g(`>`,[o(`tabs-nav`,[o(`tabs-nav-scroll-wrapper`,[g(`&::before`,`
 top: 0;
 bottom: 0;
 left: 0;
 width: 20px;
 `),g(`&::after`,`
 top: 0;
 bottom: 0;
 right: 0;
 width: 20px;
 `),v(`shadow-start`,[g(`&::before`,`
 box-shadow: inset 10px 0 8px -8px rgba(0, 0, 0, .12);
 `)]),v(`shadow-end`,[g(`&::after`,`
 box-shadow: inset -10px 0 8px -8px rgba(0, 0, 0, .12);
 `)])])])])]),v(`left, right`,[o(`tabs-nav-scroll-content`,`
 flex-direction: column;
 `),g(`>`,[o(`tabs-nav`,[o(`tabs-nav-scroll-wrapper`,[g(`&::before`,`
 top: 0;
 left: 0;
 right: 0;
 height: 20px;
 `),g(`&::after`,`
 bottom: 0;
 left: 0;
 right: 0;
 height: 20px;
 `),v(`shadow-start`,[g(`&::before`,`
 box-shadow: inset 0 10px 8px -8px rgba(0, 0, 0, .12);
 `)]),v(`shadow-end`,[g(`&::after`,`
 box-shadow: inset 0 -10px 8px -8px rgba(0, 0, 0, .12);
 `)])])])])]),o(`tabs-nav-scroll-wrapper`,`
 flex: 1;
 position: relative;
 overflow: hidden;
 `,[o(`tabs-nav-y-scroll`,`
 height: 100%;
 width: 100%;
 overflow-y: auto; 
 scrollbar-width: none;
 `,[g(`&::-webkit-scrollbar, &::-webkit-scrollbar-track-piece, &::-webkit-scrollbar-thumb`,`
 width: 0;
 height: 0;
 display: none;
 `)]),g(`&::before, &::after`,`
 transition: box-shadow .3s var(--n-bezier);
 pointer-events: none;
 content: "";
 position: absolute;
 z-index: 1;
 `),g(`&.transition-disabled`,[g(`&::before, &::after`,`
 transition: none;
 `)])]),o(`tabs-nav-scroll-content`,`
 display: flex;
 position: relative;
 min-width: 100%;
 min-height: 100%;
 width: fit-content;
 box-sizing: border-box;
 `),o(`tabs-wrapper`,`
 display: inline-flex;
 flex-wrap: nowrap;
 position: relative;
 `),o(`tabs-tab-wrapper`,`
 display: flex;
 flex-wrap: nowrap;
 flex-shrink: 0;
 flex-grow: 0;
 `),o(`tabs-tab`,`
 cursor: pointer;
 white-space: nowrap;
 flex-wrap: nowrap;
 display: inline-flex;
 align-items: center;
 color: var(--n-tab-text-color);
 font-size: var(--n-tab-font-size);
 background-clip: padding-box;
 padding: var(--n-tab-padding);
 transition:
 box-shadow .3s var(--n-bezier),
 color .3s var(--n-bezier),
 background-color .3s var(--n-bezier),
 border-color .3s var(--n-bezier);
 `,[v(`disabled`,{cursor:`not-allowed`}),l(`close`,`
 margin-inline-start: 6px;
 transition:
 background-color .3s var(--n-bezier),
 color .3s var(--n-bezier);
 `),l(`label`,`
 display: flex;
 align-items: center;
 z-index: 1;
 `)]),o(`tabs-bar`,`
 position: absolute;
 bottom: 0;
 height: 2px;
 border-radius: 1px;
 background-color: var(--n-bar-color);
 transition:
 left .2s var(--n-bezier),
 max-width .2s var(--n-bezier),
 opacity .3s var(--n-bezier),
 background-color .3s var(--n-bezier);
 `,[g(`&.transition-disabled`,`
 transition: none;
 `),v(`disabled`,`
 background-color: var(--n-tab-text-color-disabled)
 `)]),o(`tabs-pane-wrapper`,`
 position: relative;
 overflow: hidden;
 transition: max-height .2s var(--n-bezier);
 `),o(`tab-pane`,`
 color: var(--n-pane-text-color);
 width: 100%;
 transition:
 color .3s var(--n-bezier),
 background-color .3s var(--n-bezier),
 opacity .2s var(--n-bezier);
 left: 0;
 right: 0;
 top: 0;
 `,[g(`&.next-transition-leave-active, &.prev-transition-leave-active, &.next-transition-enter-active, &.prev-transition-enter-active`,`
 transition:
 color .3s var(--n-bezier),
 background-color .3s var(--n-bezier),
 transform .2s var(--n-bezier),
 opacity .2s var(--n-bezier);
 `),g(`&.next-transition-leave-active, &.prev-transition-leave-active`,`
 position: absolute;
 `),g(`&.next-transition-enter-from, &.prev-transition-leave-to`,`
 transform: translateX(32px);
 opacity: 0;
 `),g(`&.next-transition-leave-to, &.prev-transition-enter-from`,`
 transform: translateX(-32px);
 opacity: 0;
 `),g(`&.next-transition-leave-from, &.next-transition-enter-to, &.prev-transition-leave-from, &.prev-transition-enter-to`,`
 transform: translateX(0);
 opacity: 1;
 `)]),o(`tabs-tab-pad`,`
 box-sizing: border-box;
 width: var(--n-tab-gap);
 flex-grow: 0;
 flex-shrink: 0;
 `),v(`line-type, bar-type`,[o(`tabs-tab`,`
 font-weight: var(--n-tab-font-weight);
 box-sizing: border-box;
 vertical-align: bottom;
 `,[g(`&:hover`,{color:`var(--n-tab-text-color-hover)`}),v(`active`,`
 color: var(--n-tab-text-color-active);
 font-weight: var(--n-tab-font-weight-active);
 `),v(`disabled`,{color:`var(--n-tab-text-color-disabled)`})])]),o(`tabs-nav`,[l(`prefix, suffix`,`
 border-color: var(--n-tab-border-color);
 `),o(`tabs-nav-scroll-content`,`
 border-color: var(--n-tab-border-color);
 `),v(`line-type`,[v(`top`,[l(`prefix, suffix`,`
 border-bottom: 1px solid var(--n-tab-border-color);
 `),o(`tabs-nav-scroll-content`,`
 border-bottom: 1px solid var(--n-tab-border-color);
 `),o(`tabs-bar`,`
 bottom: -1px;
 `)]),v(`left`,[l(`prefix, suffix`,`
 border-right: 1px solid var(--n-tab-border-color);
 `),o(`tabs-nav-scroll-content`,`
 border-right: 1px solid var(--n-tab-border-color);
 `),o(`tabs-bar`,`
 right: -1px;
 `)]),v(`right`,[l(`prefix, suffix`,`
 border-left: 1px solid var(--n-tab-border-color);
 `),o(`tabs-nav-scroll-content`,`
 border-left: 1px solid var(--n-tab-border-color);
 `),o(`tabs-bar`,`
 left: -1px;
 `)]),v(`bottom`,[l(`prefix, suffix`,`
 border-top: 1px solid var(--n-tab-border-color);
 `),o(`tabs-nav-scroll-content`,`
 border-top: 1px solid var(--n-tab-border-color);
 `),o(`tabs-bar`,`
 top: -1px;
 `)]),l(`prefix, suffix`,`
 transition: border-color .3s var(--n-bezier);
 `),o(`tabs-nav-scroll-content`,`
 transition: border-color .3s var(--n-bezier);
 `),o(`tabs-bar`,`
 border-radius: 0;
 `)]),v(`card-type`,[l(`prefix, suffix`,`
 transition: border-color .3s var(--n-bezier);
 `),o(`tabs-pad`,`
 flex-grow: 1;
 transition: border-color .3s var(--n-bezier);
 `),o(`tabs-tab-pad`,`
 transition: border-color .3s var(--n-bezier);
 `),o(`tabs-tab`,`
 font-weight: var(--n-tab-font-weight);
 border: 1px solid var(--n-tab-border-color);
 background-color: var(--n-tab-color);
 box-sizing: border-box;
 position: relative;
 vertical-align: bottom;
 display: flex;
 justify-content: space-between;
 font-size: var(--n-tab-font-size);
 color: var(--n-tab-text-color);
 `,[v(`addable`,`
 padding-left: 8px;
 padding-right: 8px;
 font-size: 16px;
 justify-content: center;
 `,[l(`height-placeholder`,`
 width: 0;
 font-size: var(--n-tab-font-size);
 `),V(`disabled`,[g(`&:hover`,`
 color: var(--n-tab-text-color-hover);
 `)])]),v(`closable`,`padding-inline-end: 8px;`),v(`active`,`
 background-color: #0000;
 font-weight: var(--n-tab-font-weight-active);
 color: var(--n-tab-text-color-active);
 `),v(`disabled`,`color: var(--n-tab-text-color-disabled);`)])]),v(`left, right`,`
 flex-direction: column; 
 `,[l(`prefix, suffix`,`
 padding: var(--n-tab-padding-vertical);
 `),o(`tabs-wrapper`,`
 flex-direction: column;
 `),o(`tabs-tab-wrapper`,`
 flex-direction: column;
 `,[o(`tabs-tab-pad`,`
 height: var(--n-tab-gap-vertical);
 width: 100%;
 `)])]),v(`top`,[v(`card-type`,[o(`tabs-scroll-padding`,`border-bottom: 1px solid var(--n-tab-border-color);`),l(`prefix, suffix`,`
 border-bottom: 1px solid var(--n-tab-border-color);
 `),o(`tabs-tab`,`
 border-top-left-radius: var(--n-tab-border-radius);
 border-top-right-radius: var(--n-tab-border-radius);
 `,[v(`active`,`
 border-bottom: 1px solid #0000;
 `)]),o(`tabs-tab-pad`,`
 border-bottom: 1px solid var(--n-tab-border-color);
 `),o(`tabs-pad`,`
 border-bottom: 1px solid var(--n-tab-border-color);
 `)])]),v(`left`,[v(`card-type`,[o(`tabs-scroll-padding`,`border-right: 1px solid var(--n-tab-border-color);`),l(`prefix, suffix`,`
 border-right: 1px solid var(--n-tab-border-color);
 `),o(`tabs-tab`,`
 border-top-left-radius: var(--n-tab-border-radius);
 border-bottom-left-radius: var(--n-tab-border-radius);
 `,[v(`active`,`
 border-right: 1px solid #0000;
 `)]),o(`tabs-tab-pad`,`
 border-right: 1px solid var(--n-tab-border-color);
 `),o(`tabs-pad`,`
 border-right: 1px solid var(--n-tab-border-color);
 `)])]),v(`right`,[v(`card-type`,[o(`tabs-scroll-padding`,`border-left: 1px solid var(--n-tab-border-color);`),l(`prefix, suffix`,`
 border-left: 1px solid var(--n-tab-border-color);
 `),o(`tabs-tab`,`
 border-top-right-radius: var(--n-tab-border-radius);
 border-bottom-right-radius: var(--n-tab-border-radius);
 `,[v(`active`,`
 border-left: 1px solid #0000;
 `)]),o(`tabs-tab-pad`,`
 border-left: 1px solid var(--n-tab-border-color);
 `),o(`tabs-pad`,`
 border-left: 1px solid var(--n-tab-border-color);
 `)])]),v(`bottom`,[v(`card-type`,[o(`tabs-scroll-padding`,`border-top: 1px solid var(--n-tab-border-color);`),l(`prefix, suffix`,`
 border-top: 1px solid var(--n-tab-border-color);
 `),o(`tabs-tab`,`
 border-bottom-left-radius: var(--n-tab-border-radius);
 border-bottom-right-radius: var(--n-tab-border-radius);
 `,[v(`active`,`
 border-top: 1px solid #0000;
 `)]),o(`tabs-tab-pad`,`
 border-top: 1px solid var(--n-tab-border-color);
 `),o(`tabs-pad`,`
 border-top: 1px solid var(--n-tab-border-color);
 `)])])]),o(`tabs-scroll-button`,[v(`start`,`
 padding-left: 10px;
 padding-right: 6px;
 `),v(`end`,`
 padding-right: 10px;
 padding-left: 6px;
 `),v(`up`,`
 padding-bottom: 10px;
 `),v(`down`,`
 padding-top: 10px;
 `)])]),Me=D({name:`TabsButton`,props:{type:{type:String,default:`next`},mergedClsPrefix:{type:String,required:!0},vertical:Boolean,disabled:Boolean,rtl:Boolean,theme:Object,themeOverrides:Object,onClick:Function},setup(e){return{handleClick:()=>{e.disabled||e.onClick?.(e.type)}}},render(){let{mergedClsPrefix:e,disabled:t,type:n,vertical:r,rtl:i,theme:a,themeOverrides:o,handleClick:s}=this,l=n===`next`,d=r?l:i?!l:l;return y(),E(ae,{text:!0,disabled:t,size:`small`,theme:a,themeOverrides:o,onClick:s,class:c([`${e}-tabs-scroll-button`,!r&&n===`prev`&&`${e}-tabs-scroll-button--start`,!r&&n===`next`&&`${e}-tabs-scroll-button--end`,r&&n===`prev`&&`${e}-tabs-scroll-button--up`,r&&n===`next`&&`${e}-tabs-scroll-button--down`])},{icon:()=>(y(),E(u,{clsPrefix:e,style:_(r?{transform:`rotate(90deg)`}:void 0)},{default:()=>d?(y(),E(fe,{key:1})):(y(),E(de,{key:2}))},1032,[`clsPrefix`,`style`]))},1032,[`disabled`,`theme`,`themeOverrides`,`onClick`,`class`])}}),Ne=Z,Pe={...m.props,value:[String,Number],defaultValue:[String,Number],trigger:{type:String,default:`click`},type:{type:String,default:`bar`},closable:Boolean,justifyContent:String,size:String,placement:{type:String,default:`top`},tabStyle:[String,Object],tabClass:String,addTabStyle:[String,Object],addTabClass:String,barWidth:Number,paneClass:String,paneStyle:[String,Object],paneWrapperClass:String,paneWrapperStyle:[String,Object],addable:[Boolean,Object],tabsPadding:{type:Number,default:0},animated:Boolean,onBeforeLeave:Function,onAdd:Function,"onUpdate:value":[Function,Array],onUpdateValue:[Function,Array],onClose:[Function,Array],labelSize:String,activeName:[String,Number],onActiveNameChange:[Function,Array],showScrollButton:Boolean,centerActiveTab:Boolean},Fe=D({name:`Tabs`,props:Pe,slots:Object,setup(e,{slots:r}){let{mergedClsPrefixRef:o,inlineThemeDisabled:s,mergedComponentPropsRef:c,mergedRtlRef:l}=O(e),u=P(`Tabs`,l,o),f=R(()=>{let{placement:t}=e;return t===`start`?u?.value?`right`:`left`:t===`end`?u?.value?`left`:`right`:t}),p=m(`Tabs`,`-tabs`,je,me,e,o),h=x(null),g=x(null),_=x(null),v=x(null),y=x(null),b=x(null),S=x(null),C=x(!0),w=x(!0),T=ue(e,[`labelSize`,`size`]),E=R(()=>T.value?T.value:c?.value?.Tabs?.size||`medium`),D=ue(e,[`activeName`,`value`]),k=x(D.value??e.defaultValue??(r.default?re(r.default())[0]?.props?.name:null)),A=le(D,k),j={id:0},M=R(()=>{if(e.justifyContent&&e.type!==`card`)return{display:`flex`,justifyContent:e.justifyContent}});N(A,()=>{j.id=0,z(),B(()=>{V()})});function F(){let{value:e}=A;return e===null?null:h.value?.querySelector(`[data-name="${e}"]`)}function I(t){if(e.type===`card`)return;let{value:n}=_;if(!n)return;let r=n.style.opacity===`0`;if(t){let i=`${o.value}-tabs-bar--disabled`,{barWidth:a}=e,s=f.value;if(t.dataset.disabled===`true`?n.classList.add(i):n.classList.remove(i),[`top`,`bottom`].includes(s)){if(te([`top`,`maxHeight`,`height`]),typeof a==`number`&&t.offsetWidth>=a){let e=Math.floor((t.offsetWidth-a)/2)+t.offsetLeft;n.style.left=`${e}px`,n.style.maxWidth=`${a}px`}else n.style.left=`${t.offsetLeft}px`,n.style.maxWidth=`${t.offsetWidth}px`;n.style.width=`8192px`,r&&(n.style.transition=`none`),n.offsetWidth,r&&(n.style.transition=``,n.style.opacity=`1`)}else{if(te([`left`,`maxWidth`,`width`]),typeof a==`number`&&t.offsetHeight>=a){let e=Math.floor((t.offsetHeight-a)/2)+t.offsetTop;n.style.top=`${e}px`,n.style.maxHeight=`${a}px`}else n.style.top=`${t.offsetTop}px`,n.style.maxHeight=`${t.offsetHeight}px`;n.style.height=`8192px`,r&&(n.style.transition=`none`),n.offsetHeight,r&&(n.style.transition=``,n.style.opacity=`1`)}}}function ee(){if(e.type===`card`)return;let{value:t}=_;t&&(t.style.opacity=`0`)}function te(e){let{value:t}=_;if(t)for(let n of e)t.style[n]=``}function z(){if(e.type===`card`)return;let t=F();t?I(t):ee()}function ne(e,t,n,r){let i=e.getBoundingClientRect(),a=t.getBoundingClientRect(),o=n?`left`:`top`,s=n?`right`:`bottom`,c=0;r?c=(a[o]+a[s])/2-(i[o]+i[s])/2:a[o]<i[o]?c=a[o]-i[o]:a[s]>i[s]&&(c=a[s]-i[s]),c!==0&&e.scrollBy({[o]:c,behavior:`smooth`})}function V(){let t=[`top`,`bottom`].includes(f.value),n=F();if(n){if(t){let r=b.value?.$el;if(!r)return;ne(r,n,t,e.centerActiveTab)}else{let{value:r}=S;if(!r)return;ne(r,n,t,e.centerActiveTab)}}}let H=x(null),ae=0,U=null;function oe(e){let t=H.value;if(t){ae=e.getBoundingClientRect().height;let n=`${ae}px`,r=()=>{t.style.height=n,t.style.maxHeight=n};U?(r(),U(),U=null):U=r}}function ce(e){let t=H.value;if(t){let n=e.getBoundingClientRect().height,r=()=>{document.body.offsetHeight,t.style.maxHeight=`${n}px`,t.style.height=`${Math.max(ae,n)}px`};U?(U(),U=null,r()):U=r}}function de(){let t=H.value;if(t){t.style.maxHeight=``,t.style.height=``;let{paneWrapperStyle:n}=e;if(typeof n==`string`)t.style.cssText=n;else if(n){let{maxHeight:e,height:r}=n;e!==void 0&&(t.style.maxHeight=e),r!==void 0&&(t.style.height=r)}}}let fe={value:[]},pe=x(`next`);function he(e){let t=A.value,n=`next`;for(let r of fe.value){if(r===t)break;if(r===e){n=`prev`;break}}pe.value=n,ge(e)}function ge(t){let{onActiveNameChange:n,onUpdateValue:r,"onUpdate:value":i}=e;n&&W(n,t),r&&W(r,t),i&&W(i,t),k.value=t}function _e(t){let{onClose:n}=e;n&&W(n,t)}function ve(e){if([`top`,`bottom`].includes(f.value)){let{value:t}=b;if(!t)return;let n=t.$el;if(!n)return;let r=n.offsetWidth,i=!!u?.value,a=e===`next`?r:-r;n.scrollBy({left:i?-a:a,behavior:`smooth`})}else{let{value:t}=S;if(!t)return;let n=t.offsetHeight,r=e===`next`?t.scrollTop+n:t.scrollTop-n;t.scrollTo({top:r,left:0,behavior:`smooth`})}}let G=!0;function K(){let{value:e}=_;if(!e)return;G&&=!1;let t=`transition-disabled`;e.classList.add(t),z(),e.classList.remove(t)}let q=x(null);function J({transitionDisabled:e}){let t=h.value;if(!t)return;e&&t.classList.add(`transition-disabled`);let n=F();n&&q.value&&(q.value.style.width=`${n.offsetWidth}px`,q.value.style.height=`${n.offsetHeight}px`,q.value.style.transform=`translate(${n.offsetLeft}px, ${n.offsetTop}px)`,e&&q.value.offsetWidth),e&&t.classList.remove(`transition-disabled`)}N([A],()=>{e.type===`segment`&&B(()=>{J({transitionDisabled:!1})})}),n(()=>{e.type===`segment`&&J({transitionDisabled:!0})});let ye=0;function be(t){if(t.contentRect.width===0&&t.contentRect.height===0||ye===t.contentRect.width)return;ye=t.contentRect.width;let{type:n}=e;(n===`line`||n===`bar`)&&(G||e.justifyContent?.startsWith(`space`))&&K(),n!==`segment`&&Q(Te())}let Y=Ne(be,64);function xe(){let{type:t}=e;t===`line`||t===`bar`?K():t===`segment`&&J({transitionDisabled:!0})}N([()=>e.justifyContent,()=>e.size],()=>{B(()=>{(e.type===`line`||e.type===`bar`)&&K()})}),N([f,()=>u?.value],()=>{B(()=>{xe(),Q(Te(),{instantly:!0})})}),N(()=>e.type,()=>{B(()=>{let e=g.value;e&&(e.classList.add(`transition-disabled`),xe(),e.offsetWidth,e.classList.remove(`transition-disabled`))})});let X=x(!1);function Se(e){let{target:t,contentRect:{width:n,height:r}}=e,i=t.parentElement.parentElement.offsetWidth,a=t.parentElement.parentElement.offsetHeight,o=f.value;if(!X.value)o===`top`||o===`bottom`?i<n&&(X.value=!0):a<r&&(X.value=!0);else{let{value:e}=y;if(!e)return;o===`top`||o===`bottom`?i-n>e.$el.offsetWidth&&(X.value=!1):a-r>e.$el.offsetHeight&&(X.value=!1)}Q(b.value?.$el||null)}let Ce=Ne(Se,64);function we(){let{onAdd:t}=e;t&&t()}let Z=x(!1);function Te(){let e=f.value;return(e===`top`||e===`bottom`?b.value?.$el:S.value)||null}function Q(e,t={instantly:!1}){if(!e)return;let n=t.instantly?v.value:null;n&&n.classList.add(`transition-disabled`);let r=f.value;if(r===`top`||r===`bottom`){let{scrollLeft:t,scrollWidth:n,offsetWidth:r}=e,i=Math.abs(t);C.value=i<=1,w.value=i+r>=n-1,Z.value=r<n-1}else{let{scrollTop:t,scrollHeight:n,offsetHeight:r}=e;C.value=t<=1,w.value=t+r>=n-1,Z.value=r<n-1}n&&(n.offsetWidth,n.classList.remove(`transition-disabled`))}let De=Ne(e=>{Q(e.target)},64);i(Ee,{triggerRef:L(e,`trigger`),tabStyleRef:L(e,`tabStyle`),tabClassRef:L(e,`tabClass`),addTabStyleRef:L(e,`addTabStyle`),addTabClassRef:L(e,`addTabClass`),paneClassRef:L(e,`paneClass`),paneStyleRef:L(e,`paneStyle`),mergedClsPrefixRef:o,typeRef:L(e,`type`),closableRef:L(e,`closable`),valueRef:A,tabChangeIdRef:j,onBeforeLeaveRef:L(e,`onBeforeLeave`),activateTab:he,handleClose:_e,handleAdd:we}),se(()=>{z(),V()}),d(()=>{let{value:e}=v;if(!e)return;let{value:t}=o,n=`${t}-tabs-nav-scroll-wrapper--shadow-start`,r=`${t}-tabs-nav-scroll-wrapper--shadow-end`;C.value?e.classList.remove(n):e.classList.add(n),w.value?e.classList.remove(r):e.classList.add(r)});let Oe={syncBarPosition:()=>{z()},scrollToCurrentTab:()=>{V()}},ke=()=>{J({transitionDisabled:!0})},Ae=R(()=>{let{value:n}=E,{type:r}=e,i=`${n}${{card:`Card`,bar:`Bar`,line:`Line`,segment:`Segment`}[r]}`,{self:{barColor:a,closeIconColor:o,closeIconColorHover:s,closeIconColorPressed:c,tabColor:l,tabBorderColor:u,paneTextColor:d,tabFontWeight:f,tabBorderRadius:m,tabFontWeightActive:h,colorSegment:g,fontWeightStrong:_,tabColorSegment:v,closeSize:y,closeIconSize:b,closeColorHover:x,closeColorPressed:S,closeBorderRadius:C,[t(`panePadding`,n)]:w,[t(`tabPadding`,i)]:T,[t(`tabPaddingVertical`,i)]:D,[t(`tabGap`,i)]:O,[t(`tabGap`,`${i}Vertical`)]:k,[t(`tabTextColor`,r)]:A,[t(`tabTextColorActive`,r)]:j,[t(`tabTextColorHover`,r)]:M,[t(`tabTextColorDisabled`,r)]:N,[t(`tabFontSize`,n)]:P},common:{cubicBezierEaseInOut:F}}=p.value;return{"--n-bezier":F,"--n-color-segment":g,"--n-bar-color":a,"--n-tab-font-size":P,"--n-tab-text-color":A,"--n-tab-text-color-active":j,"--n-tab-text-color-disabled":N,"--n-tab-text-color-hover":M,"--n-pane-text-color":d,"--n-tab-border-color":u,"--n-tab-border-radius":m,"--n-close-size":y,"--n-close-icon-size":b,"--n-close-color-hover":x,"--n-close-color-pressed":S,"--n-close-border-radius":C,"--n-close-icon-color":o,"--n-close-icon-color-hover":s,"--n-close-icon-color-pressed":c,"--n-tab-color":l,"--n-tab-font-weight":f,"--n-tab-font-weight-active":h,"--n-tab-padding":T,"--n-tab-padding-vertical":D,"--n-tab-gap":O,"--n-tab-gap-vertical":k,"--n-pane-padding-left":ie(w,`left`),"--n-pane-padding-right":ie(w,`right`),"--n-pane-padding-top":ie(w,`top`),"--n-pane-padding-bottom":ie(w,`bottom`),"--n-font-weight-strong":_,"--n-tab-color-segment":v}}),$=s?a(`tabs`,R(()=>`${E.value[0]}${e.type[0]}`),Ae,e):void 0;return{mergedClsPrefix:o,mergedValue:A,renderedNames:new Set,segmentCapsuleElRef:q,tabsPaneWrapperRef:H,tabsElRef:h,selfElRef:g,barElRef:_,addTabInstRef:y,xScrollInstRef:b,scrollWrapperElRef:v,addTabFixed:X,tabWrapperStyle:M,handleNavResize:Y,mergedSize:E,handleScroll:De,handleTabsResize:Ce,cssVars:s?void 0:Ae,themeClass:$?.themeClass,animationDirection:pe,renderNameListRef:fe,yScrollElRef:S,handleSegmentResize:ke,onAnimationBeforeLeave:oe,onAnimationEnter:ce,onAnimationAfterEnter:de,onRender:$?.onRender,startReachedRef:C,endReachedRef:w,isOverflow:Z,handleButtonClick:ve,mergedTheme:p,rtlEnabled:u,mergedPlacement:f,...Oe}},render(){let{mergedClsPrefix:t,type:n,mergedPlacement:r,addTabFixed:i,addable:a,mergedSize:o,renderNameListRef:l,onRender:u,paneWrapperClass:d,paneWrapperStyle:f,startReachedRef:p,endReachedRef:m,isOverflow:h,showScrollButton:g,handleButtonClick:v,mergedTheme:b,rtlEnabled:x,$slots:{default:C,prefix:w,suffix:D}}=this;u?.();let O=C?re(C()).filter(e=>e.type.__TAB_PANE__===!0):[],k=C?re(C()).filter(e=>e.type.__TAB__===!0):[],A=!k.length,M=n===`card`,N=n===`segment`,P=!M&&!N&&this.justifyContent;l.value=[];let F=()=>{let n=(y(),z(`div`,{style:_(this.tabWrapperStyle),class:c(`${t}-tabs-wrapper`)},[P?S(()=>null):(y(),z(`div`,{key:1,class:c(`${t}-tabs-scroll-padding`),style:_(r===`top`||r===`bottom`?{width:`${this.tabsPadding}px`}:{height:`${this.tabsPadding}px`})},null,6)),A?(y(),z(e,{key:2},[S(()=>O.map((e,t)=>(l.value.push(e.props.name),ze((y(),E($,T(e.props,{internalCreatedByPane:!0,internalLeftPadded:t!==0&&(!P||P===`center`||P===`start`||P===`end`)}),s(e.children?{default:e.children.tab}:void 0),1040,[`internalLeftPadded`]))))))],64)):(y(),z(e,{key:3},[S(()=>k.map((e,t)=>(l.value.push(e.props.name),ze(t!==0&&!P?Re(e):e))))],64)),!i&&a&&M?(y(),z(e,{key:4},[S(()=>Le(a,(A?O.length:k.length)!==0))],64)):S(()=>null),P?S(()=>null):(y(),z(`div`,{key:7,class:c(`${t}-tabs-scroll-padding`),style:_({width:`${this.tabsPadding}px`})},null,6)),M?S(()=>null):(y(),z(`div`,{key:9,ref:`barElRef`,class:c(`${t}-tabs-bar`)},null,2))],6));return y(),z(`div`,{ref:`tabsElRef`,class:c(`${t}-tabs-nav-scroll-content`)},[M&&a?(y(),E(U,{key:0,onResize:this.handleTabsResize},{default:()=>n},1032,[`onResize`])):(y(),z(e,{key:1},[S(()=>n)],64)),M?(y(),z(`div`,{key:2,class:c(`${t}-tabs-pad`)},null,2)):S(()=>null)],2)},I=N?`top`:r;return y(),z(`div`,{ref:`selfElRef`,class:c([`${t}-tabs`,this.themeClass,`${t}-tabs--${n}-type`,`${t}-tabs--${o}-size`,P&&`${t}-tabs--flex`,`${t}-tabs--${I}`,x&&`${t}-tabs--rtl`]),style:_(this.cssVars)},[j(`div`,{class:c([`${t}-tabs-nav--${n}-type`,`${t}-tabs-nav--${I}`,`${t}-tabs-nav`])},[S(()=>H(w,e=>e&&(y(),z(`div`,{class:c(`${t}-tabs-nav__prefix`)},[S(()=>e)],2)))),N?(y(),E(U,{key:0,onResize:this.handleSegmentResize},{default:()=>(y(),z(`div`,{class:c(`${t}-tabs-rail`),ref:`tabsElRef`},[j(`div`,{class:c(`${t}-tabs-capsule`),ref:`segmentCapsuleElRef`},[j(`div`,{class:c(`${t}-tabs-wrapper`)},[j(`div`,{class:c(`${t}-tabs-tab`)},null,2)],2)],2),A?(y(),z(e,{key:0},[S(()=>O.map((e,t)=>(l.value.push(e.props.name),y(),E($,T(e.props,{internalCreatedByPane:!0,internalLeftPadded:t!==0}),s(e.children?{default:e.children.tab}:void 0),1040,[`internalLeftPadded`]))))],64)):(y(),z(e,{key:1},[S(()=>k.map((e,t)=>(l.value.push(e.props.name),t===0?e:Re(e))))],64))],2))},1032,[`onResize`])):(y(),z(e,{key:1},[S(()=>g&&h&&(y(),E(Me,{mergedClsPrefix:t,type:`prev`,vertical:I===`left`||I===`right`,disabled:p,rtl:!!x,theme:b.peers.Button,themeOverrides:b.peerOverrides.Button,onClick:v},null,8,[`mergedClsPrefix`,`vertical`,`disabled`,`rtl`,`theme`,`themeOverrides`,`onClick`]))),(y(),E(U,{onResize:this.handleNavResize},{default:()=>(y(),z(`div`,{class:c(`${t}-tabs-nav-scroll-wrapper`),ref:`scrollWrapperElRef`},[[`top`,`bottom`].includes(I)?(y(),E(Q,{key:0,ref:`xScrollInstRef`,onScroll:this.handleScroll},{default:F},1032,[`onScroll`])):(y(),z(`div`,{key:1,class:c(`${t}-tabs-nav-y-scroll`),onScroll:this.handleScroll,ref:`yScrollElRef`},[S(()=>F())],42,[`onScroll`]))],2))},1032,[`onResize`])),S(()=>g&&h&&(y(),E(Me,{mergedClsPrefix:t,type:`next`,vertical:I===`left`||I===`right`,disabled:m,rtl:!!x,theme:b.peers.Button,themeOverrides:b.peerOverrides.Button,onClick:v},null,8,[`mergedClsPrefix`,`vertical`,`disabled`,`rtl`,`theme`,`themeOverrides`,`onClick`])))],64)),i&&a&&M?(y(),z(e,{key:2},[S(()=>Le(a,!0))],64)):S(()=>null),S(()=>H(D,e=>e&&(y(),z(`div`,{class:c(`${t}-tabs-nav__suffix`)},[S(()=>e)],2))))],2),S(()=>A&&(this.animated&&(I===`top`||I===`bottom`)?(y(),z(`div`,{key:1,ref:`tabsPaneWrapperRef`,style:_(f),class:c([`${t}-tabs-pane-wrapper`,d])},[S(()=>Ie(O,this.mergedValue,this.renderedNames,this.onAnimationBeforeLeave,this.onAnimationEnter,this.onAnimationAfterEnter,this.animationDirection))],6)):Ie(O,this.mergedValue,this.renderedNames)))],6)}});function Ie(e,t,n,r,i,a,o){let s=[];return e.forEach(e=>{let{name:r,displayDirective:i,"display-directive":a}=e.props,o=e=>i===e||a===e,c=t===r;if(e.key!==void 0&&(e.key=r),c||o(`show`)||o(`show:lazy`)&&n.has(r)){n.has(r)||n.add(r);let t=!o(`if`);s.push(t?h(e,[[C,c]]):e)}}),o?(y(),E(ee,{name:`${o}-transition`,onBeforeLeave:r,onEnter:i,onAfterEnter:a},{default:()=>s},1032,[`name`,`onBeforeLeave`,`onEnter`,`onAfterEnter`])):s}function Le(e,t){return y(),E($,{ref:`addTabInstRef`,key:`__addable`,name:`__addable`,internalCreatedByPane:!0,internalAddable:!0,internalLeftPadded:t,disabled:typeof e==`object`&&e.disabled},null,8,[`internalLeftPadded`,`disabled`])}function Re(e){let t=F(e);return t.props?t.props.internalLeftPadded=!0:t.props={internalLeftPadded:!0},t}function ze(e){return Array.isArray(e.dynamicProps)?e.dynamicProps.includes(`internalLeftPadded`)||e.dynamicProps.push(`internalLeftPadded`):e.dynamicProps=[`internalLeftPadded`],e}export{Oe as n,Fe as t};