import{$t as e,Bt as t,En as n,F as r,Ft as i,Hn as a,K as o,Kt as s,Lt as c,P as l,Pt as u,Rt as d,Tn as f,Un as p,Vn as m,Y as h,_n as g,an as _,at as v,bn as y,dn as b,et as x,g as S,hn as C,in as w,j as T,jn as E,pn as D,q as O,qn as k,rn as A,rt as j,sn as M,xn as N,zt as P}from"./light-TBGKFU-e.js";import{$ as F,D as I,J as L,Q as R,W as z,Z as B,et as ee,q as V,tt as te,y as ne}from"./request-BS4zoC18.js";import{a as H,c as re,i as U,l as ie,r as W,s as ae,t as oe}from"./create-G_I1UjLa.js";import{t as G}from"./happens-in-D4nrdV77.js";import{t as se}from"./use-merged-state-Cqr2carY.js";import{t as ce}from"./ChevronRight-CqswQINz.js";import{i as le,o as ue}from"./light-C-sfhqWC.js";import{t as de}from"./create-ref-setter-AIudcg5Z.js";import{t as fe}from"./Icon-BEbAVPHB.js";function pe(e={},t){let n=m({ctrl:!1,command:!1,win:!1,shift:!1,tab:!1}),{keydown:r,keyup:i}=e,o=e=>{switch(e.key){case`Control`:n.ctrl=!0;break;case`Meta`:n.command=!0,n.win=!0;break;case`Shift`:n.shift=!0;break;case`Tab`:n.tab=!0}r!==void 0&&Object.keys(r).forEach(t=>{if(t!==e.key)return;let n=r[t];if(typeof n==`function`)n(e);else{let{stop:t=!1,prevent:r=!1}=n;t&&e.stopPropagation(),r&&e.preventDefault(),n.handler(e)}})},s=e=>{switch(e.key){case`Control`:n.ctrl=!1;break;case`Meta`:n.command=!1,n.win=!1;break;case`Shift`:n.shift=!1;break;case`Tab`:n.tab=!1}i!==void 0&&Object.keys(i).forEach(t=>{if(t!==e.key)return;let n=i[t];if(typeof n==`function`)n(e);else{let{stop:t=!1,prevent:r=!1}=n;t&&e.stopPropagation(),r&&e.preventDefault(),n.handler(e)}})},c=()=>{(t===void 0||t.value)&&(R(`keydown`,document,o),R(`keyup`,document,s)),t!==void 0&&E(t,e=>{e?(R(`keydown`,document,o),R(`keyup`,document,s)):(B(`keydown`,document,o),B(`keyup`,document,s))})};return L()?(y(c),N(()=>{(t===void 0||t.value)&&(B(`keydown`,document,o),B(`keyup`,document,s))})):c(),a(n)}var me={...U,...l.props},he=b({name:`Tooltip`,props:me,slots:Object,__popover__:!0,setup(e){let{mergedClsPrefixRef:t}=x(e),n=l(`Tooltip`,`-tooltip`,void 0,le,e,t),r=p(null);return{syncPosition(){r.value.syncPosition()},setShow(e){r.value.setShow(e)},popoverRef:r,mergedTheme:n,popoverThemeOverrides:A(()=>n.value.self)}},render(){let{mergedTheme:e,internalExtraClass:t}=this;return D(W,{...this.$props,theme:e.peers.Popover,themeOverrides:e.peerOverrides.Popover,builtinThemeOverrides:this.popoverThemeOverrides,internalExtraClass:t.concat(`tooltip`),ref:`popoverRef`},this.$slots)}}),K=j(`n-dropdown-menu`),q=j(`n-dropdown`),J=j(`n-dropdown-option`),Y=b({name:`DropdownDivider`,props:{clsPrefix:{type:String,required:!0}},render(){return f(),M(`div`,{class:o(`${this.clsPrefix}-dropdown-divider`)},null,2)}});function X(e,t){return e.type===`submenu`||e.type===void 0&&e[t]!==void 0}function ge(e){return e.type===`group`}function Z(e){return e.type===`divider`}function _e(e){return e.type===`render`}function ve(e,t,n){if(!t)return e;let r=p(e.value),i=null;return E(e,e=>{i!==null&&window.clearTimeout(i),e===!0?n&&!n.value?r.value=!0:i=window.setTimeout(()=>{r.value=!0},t):r.value=!1}),r}var Q=b({name:`DropdownOption`,props:{clsPrefix:{type:String,required:!0},tmNode:{type:Object,required:!0},parentKey:{type:[String,Number],default:null},placement:{type:String,default:`right-start`},props:Object,scrollable:Boolean},setup(e){let t=C(q),{hoverKeyRef:r,keyboardKeyRef:i,lastToggledSubmenuKeyRef:a,pendingKeyPathRef:o,activeKeyPathRef:s,animatedRef:c,mergedShowRef:l,renderLabelRef:u,renderIconRef:d,labelFieldRef:f,childrenFieldRef:m,renderOptionRef:h,nodePropsRef:g,menuPropsRef:_}=t,v=C(J,null),y=C(K),b=C(F),x=A(()=>e.tmNode.rawNode),S=A(()=>{let{value:t}=m;return X(e.tmNode.rawNode,t)}),w=A(()=>{let{disabled:t}=e.tmNode;return t}),E=ve(A(()=>{if(!S.value)return!1;let{key:t,disabled:n}=e.tmNode;if(n)return!1;let{value:s}=r,{value:c}=i,{value:l}=a,{value:u}=o;return s===null?c===null?l!==null&&u.includes(t):u.includes(t)&&u[u.length-1]!==t:u.includes(t)}),300,A(()=>i.value===null&&!c.value)),D=A(()=>!!v?.enteringSubmenuRef.value),O=p(!1);n(J,{enteringSubmenuRef:O});function k(){O.value=!0}function j(){O.value=!1}function M(){let{parentKey:t,tmNode:n}=e;n.disabled||l.value&&(a.value=t,i.value=null,r.value=n.key)}function N(){let{tmNode:t}=e;t.disabled||l.value&&r.value!==t.key&&M()}function P(t){if(e.tmNode.disabled||!l.value)return;let{relatedTarget:n}=t;n&&!G({target:n},`dropdownOption`)&&!G({target:n},`scrollbarRail`)&&(r.value=null)}function I(){let{value:n}=S,{tmNode:r}=e;l.value&&!n&&!r.disabled&&(t.doSelect(r.key,r.rawNode),t.doUpdateShow(!1))}return{labelField:f,renderLabel:u,renderIcon:d,siblingHasIcon:y.showIconRef,siblingHasSubmenu:y.hasSubmenuRef,menuProps:_,popoverBody:b,animated:c,mergedShowSubmenu:A(()=>E.value&&!D.value),rawNode:x,hasSubmenu:S,pending:T(()=>{let{value:t}=o,{key:n}=e.tmNode;return t.includes(n)}),childActive:T(()=>{let{value:t}=s,{key:n}=e.tmNode,r=t.findIndex(e=>n===e);return r!==-1&&r<t.length-1}),active:T(()=>{let{value:t}=s,{key:n}=e.tmNode,r=t.findIndex(e=>n===e);return r!==-1&&r===t.length-1}),mergedDisabled:w,renderOption:h,nodeProps:g,handleClick:I,handleMouseMove:N,handleMouseEnter:M,handleMouseLeave:P,handleSubmenuBeforeEnter:k,handleSubmenuAfterEnter:j}},render(){let{animated:t,rawNode:n,mergedShowSubmenu:r,clsPrefix:i,siblingHasIcon:a,siblingHasSubmenu:c,renderLabel:l,renderIcon:u,renderOption:d,nodeProps:p,props:m,scrollable:v}=this,y=null;if(r){let e=this.menuProps?.(n,n.children);y=(t=>(f(),_($,g({key:1},e,{clsPrefix:i,scrollable:this.scrollable,tmNodes:this.tmNode.children,parentKey:this.tmNode.key}),null,16,[`clsPrefix`,`scrollable`,`tmNodes`,`parentKey`])))(y)}let b={class:[`${i}-dropdown-option-body`,this.pending&&`${i}-dropdown-option-body--pending`,this.active&&`${i}-dropdown-option-body--active`,this.childActive&&`${i}-dropdown-option-body--child-active`,this.mergedDisabled&&`${i}-dropdown-option-body--disabled`],onMousemove:this.handleMouseMove,onMouseenter:this.handleMouseEnter,onMouseleave:this.handleMouseLeave,onClick:this.handleClick},x=p?.(n),C=(f(),M(`div`,g({class:[`${i}-dropdown-option`,x?.class],"data-dropdown-option":!0},x),[h(()=>D(`div`,g(b,m),[(f(),M(`div`,{class:o([`${i}-dropdown-option-body__prefix`,a&&`${i}-dropdown-option-body__prefix--show-icon`])},[h(()=>[u?u(n):S(n.icon)])],2)),(f(),M(`div`,{"data-dropdown-option":!0,class:o(`${i}-dropdown-option-body__label`)},[l?(f(),M(e,{key:0},[h(()=>l(n))],64)):(f(),M(e,{key:1},[h(()=>S(n[this.labelField]??n.title))],64))],2)),(f(),M(`div`,{"data-dropdown-option":!0,class:o([`${i}-dropdown-option-body__suffix`,c&&`${i}-dropdown-option-body__suffix--has-submenu`])},[this.hasSubmenu?(f(),_(fe,{key:0},{_:1,default:O(()=>(f(),_(ce)))})):h(()=>null)],2))])),this.hasSubmenu?(f(),_(ie,{key:0},{default:()=>[(f(),_(re,null,{default:()=>(f(),M(`div`,{class:o(`${i}-dropdown-offset-container`)},[(f(),_(ae,{show:this.mergedShowSubmenu,placement:this.placement,to:v&&this.popoverBody||void 0,teleportDisabled:!v},{default:()=>(f(),M(`div`,{class:o(`${i}-dropdown-menu-wrapper`)},[t?(f(),_(s,{key:0,onBeforeEnter:this.handleSubmenuBeforeEnter,onAfterEnter:this.handleSubmenuAfterEnter,name:`fade-in-scale-up-transition`,appear:!0},{default:()=>y},1032,[`onBeforeEnter`,`onAfterEnter`])):(f(),M(e,{key:1},[h(()=>y)],64))],2))},1032,[`show`,`placement`,`to`,`teleportDisabled`]))],2))},1024))]},1024)):h(()=>null)],16));return d?d({node:C,option:n}):C}}),ye=b({name:`DropdownGroupHeader`,props:{clsPrefix:{type:String,required:!0},tmNode:{type:Object,required:!0}},setup(){let{showIconRef:e,hasSubmenuRef:t}=C(K),{renderLabelRef:n,labelFieldRef:r,nodePropsRef:i,renderOptionRef:a}=C(q);return{labelField:r,showIcon:e,hasSubmenu:t,renderLabel:n,nodeProps:i,renderOption:a}},render(){let{clsPrefix:t,hasSubmenu:n,showIcon:r,nodeProps:i,renderLabel:a,renderOption:s}=this,{rawNode:c}=this.tmNode,l=(f(),M(`div`,g({class:`${t}-dropdown-option`},i?.(c)),[w(`div`,{class:o(`${t}-dropdown-option-body ${t}-dropdown-option-body--group`)},[w(`div`,{"data-dropdown-option":!0,class:o([`${t}-dropdown-option-body__prefix`,r&&`${t}-dropdown-option-body__prefix--show-icon`])},[h(()=>S(c.icon))],2),w(`div`,{class:o(`${t}-dropdown-option-body__label`),"data-dropdown-option":!0},[a?(f(),M(e,{key:0},[h(()=>a(c))],64)):(f(),M(e,{key:1},[h(()=>S(c.title??c[this.labelField]))],64))],2),w(`div`,{class:o([`${t}-dropdown-option-body__suffix`,n&&`${t}-dropdown-option-body__suffix--has-submenu`]),"data-dropdown-option":!0},null,2)],2)],16));return s?s({node:l,option:c}):l}}),be=b({name:`NDropdownGroup`,props:{clsPrefix:{type:String,required:!0},tmNode:{type:Object,required:!0},parentKey:{type:[String,Number],default:null}},render(){let{tmNode:t,parentKey:n,clsPrefix:r}=this,{children:i}=t;return f(),M(e,null,[(f(),_(ye,{clsPrefix:r,tmNode:t,key:t.key},null,8,[`clsPrefix`,`tmNode`])),h(()=>i?.map(e=>{let{rawNode:t}=e;return t.show===!1?null:Z(t)?D(Y,{clsPrefix:r,key:e.key}):e.isGroup?(v(`dropdown`,"`group` node is not allowed to be put in `group` node."),null):(f(),_(Q,{clsPrefix:r,tmNode:e,parentKey:n,key:e.key},null,8,[`clsPrefix`,`tmNode`,`parentKey`]))}))],64)}}),xe=b({name:`DropdownRenderOption`,props:{tmNode:{type:Object,required:!0}},render(){let{rawNode:{render:e,props:t}}=this.tmNode;return D(`div`,t,[e?.()])}}),$=b({name:`DropdownMenu`,props:{scrollable:Boolean,showArrow:Boolean,arrowStyle:[String,Object],clsPrefix:{type:String,required:!0},tmNodes:{type:Array,default:()=>[]},parentKey:{type:[String,Number],default:null}},setup(e){let{renderIconRef:t,childrenFieldRef:r}=C(q);n(K,{showIconRef:A(()=>{let n=t.value;return e.tmNodes.some(e=>{if(e.isGroup)return e.children?.some(({rawNode:e})=>n?n(e):e.icon);let{rawNode:t}=e;return n?n(t):t.icon})}),hasSubmenuRef:A(()=>{let{value:t}=r;return e.tmNodes.some(e=>{if(e.isGroup)return e.children?.some(({rawNode:e})=>X(e,t));let{rawNode:n}=e;return X(n,t)})})});let i=p(null);return n(ee,null),n(te,null),n(F,i),{bodyRef:i}},render(){let{parentKey:t,clsPrefix:n,scrollable:r}=this,i=this.tmNodes.map(e=>{let{rawNode:i}=e;return i.show===!1?null:_e(i)?(f(),_(xe,{tmNode:e,key:e.key},null,8,[`tmNode`])):Z(i)?(f(),_(Y,{clsPrefix:n,key:e.key},null,8,[`clsPrefix`])):ge(i)?(f(),_(be,{clsPrefix:n,tmNode:e,parentKey:t,key:e.key},null,8,[`clsPrefix`,`tmNode`,`parentKey`])):(f(),_(Q,{clsPrefix:n,tmNode:e,parentKey:t,key:e.key,props:i.props,scrollable:r},null,8,[`clsPrefix`,`tmNode`,`parentKey`,`props`,`scrollable`]))});return f(),M(`div`,{class:o([`${n}-dropdown-menu`,r&&`${n}-dropdown-menu--scrollable`]),ref:`bodyRef`},[r?(f(),_(I,{key:0,contentClass:`${n}-dropdown-menu__content`},{default:()=>i},1032,[`contentClass`])):(f(),M(e,{key:1},[h(()=>i)],64)),this.showArrow?(f(),M(e,{key:2},[h(()=>H({clsPrefix:n,arrowStyle:this.arrowStyle,arrowClass:void 0,arrowWrapperClass:void 0,arrowWrapperStyle:void 0}))],64)):h(()=>null)],2)}}),Se=i(`dropdown-menu`,`
 transform-origin: var(--v-transform-origin);
 background-color: var(--n-color);
 border-radius: var(--n-border-radius);
 box-shadow: var(--n-box-shadow);
 position: relative;
 transition:
 background-color .3s var(--n-bezier),
 box-shadow .3s var(--n-bezier);
`,[ne(),i(`dropdown-option`,`
 position: relative;
 `,[u(`a`,`
 text-decoration: none;
 color: inherit;
 outline: none;
 `,[u(`&::before`,`
 content: "";
 position: absolute;
 left: 0;
 right: 0;
 top: 0;
 bottom: 0;
 `)]),i(`dropdown-option-body`,`
 display: flex;
 cursor: pointer;
 position: relative;
 height: var(--n-option-height);
 line-height: var(--n-option-height);
 font-size: var(--n-font-size);
 color: var(--n-option-text-color);
 transition: color .3s var(--n-bezier);
 `,[u(`&::before`,`
 content: "";
 position: absolute;
 top: 0;
 bottom: 0;
 left: 4px;
 right: 4px;
 transition: background-color .3s var(--n-bezier);
 border-radius: var(--n-border-radius);
 `),P(`disabled`,[d(`pending`,`
 color: var(--n-option-text-color-hover);
 `,[c(`prefix, suffix`,`
 color: var(--n-option-text-color-hover);
 `),u(`&::before`,`background-color: var(--n-option-color-hover);`)]),d(`active`,`
 color: var(--n-option-text-color-active);
 `,[c(`prefix, suffix`,`
 color: var(--n-option-text-color-active);
 `),u(`&::before`,`background-color: var(--n-option-color-active);`)]),d(`child-active`,`
 color: var(--n-option-text-color-child-active);
 `,[c(`prefix, suffix`,`
 color: var(--n-option-text-color-child-active);
 `)])]),d(`disabled`,`
 cursor: not-allowed;
 opacity: var(--n-option-opacity-disabled);
 `),d(`group`,`
 font-size: calc(var(--n-font-size) - 1px);
 color: var(--n-group-header-text-color);
 `,[c(`prefix`,`
 width: calc(var(--n-option-prefix-width) / 2);
 `,[d(`show-icon`,`
 width: calc(var(--n-option-icon-prefix-width) / 2);
 `)])]),c(`prefix`,`
 width: var(--n-option-prefix-width);
 display: flex;
 justify-content: center;
 align-items: center;
 color: var(--n-prefix-color);
 transition: color .3s var(--n-bezier);
 z-index: 1;
 `,[d(`show-icon`,`
 width: var(--n-option-icon-prefix-width);
 `),i(`icon`,`
 font-size: var(--n-option-icon-size);
 `)]),c(`label`,`
 white-space: nowrap;
 flex: 1;
 z-index: 1;
 `),c(`suffix`,`
 box-sizing: border-box;
 flex-grow: 0;
 flex-shrink: 0;
 display: flex;
 justify-content: flex-end;
 align-items: center;
 min-width: var(--n-option-suffix-width);
 padding: 0 8px;
 transition: color .3s var(--n-bezier);
 color: var(--n-suffix-color);
 z-index: 1;
 `,[d(`has-submenu`,`
 width: var(--n-option-icon-suffix-width);
 `),i(`icon`,`
 font-size: var(--n-option-icon-size);
 `)]),i(`dropdown-menu`,`pointer-events: all;`)]),i(`dropdown-offset-container`,`
 pointer-events: none;
 position: absolute;
 left: 0;
 right: 0;
 top: -4px;
 bottom: -4px;
 `)]),i(`dropdown-divider`,`
 transition: background-color .3s var(--n-bezier);
 background-color: var(--n-divider-color);
 height: 1px;
 margin: 4px 0;
 `),i(`dropdown-menu-wrapper`,`
 transform-origin: var(--v-transform-origin);
 width: fit-content;
 `),u(`>`,[i(`scrollbar`,`
 height: inherit;
 max-height: inherit;
 `)]),P(`scrollable`,`
 padding: var(--n-padding);
 `),d(`scrollable`,[c(`content`,`
 padding: var(--n-padding);
 `)])]),Ce={animated:{type:Boolean,default:!0},keyboard:{type:Boolean,default:!0},size:String,inverted:Boolean,placement:{type:String,default:`bottom`},onSelect:[Function,Array],options:{type:Array,default:()=>[]},menuProps:Function,showArrow:Boolean,renderLabel:Function,renderIcon:Function,renderOption:Function,nodeProps:Function,labelField:{type:String,default:`label`},keyField:{type:String,default:`key`},childrenField:{type:String,default:`children`},value:[String,Number]},we=Object.keys(U),Te={...U,...Ce,...l.props},Ee=b({name:`Dropdown`,inheritAttrs:!1,props:Te,setup(e){let i=p(!1),a=se(k(e,`show`),i),o=A(()=>{let{keyField:t,childrenField:n}=e;return oe(e.options,{getKey(e){return e[t]},getDisabled(e){return e.disabled===!0},getIgnored(e){return e.type===`divider`||e.type===`render`},getChildren(e){return e[n]}})}),s=A(()=>o.value.treeNodes),c=p(null),u=p(null),d=p(null),f=A(()=>c.value??u.value??d.value??null),m=A(()=>o.value.getPath(f.value).keyPath),h=A(()=>o.value.getPath(e.value).keyPath),g=T(()=>e.keyboard&&a.value);pe({keydown:{ArrowUp:{prevent:!0,handler:N},ArrowRight:{prevent:!0,handler:M},ArrowDown:{prevent:!0,handler:P},ArrowLeft:{prevent:!0,handler:j},Enter:{prevent:!0,handler:F},Escape:O}},g);let{mergedClsPrefixRef:_,inlineThemeDisabled:v,mergedComponentPropsRef:y}=x(e),b=A(()=>e.size||y?.value?.Dropdown?.size||`medium`),S=l(`Dropdown`,`-dropdown`,Se,ue,e,_);n(q,{labelFieldRef:k(e,`labelField`),childrenFieldRef:k(e,`childrenField`),renderLabelRef:k(e,`renderLabel`),renderIconRef:k(e,`renderIcon`),hoverKeyRef:c,keyboardKeyRef:u,lastToggledSubmenuKeyRef:d,pendingKeyPathRef:m,activeKeyPathRef:h,animatedRef:k(e,`animated`),mergedShowRef:a,nodePropsRef:k(e,`nodeProps`),renderOptionRef:k(e,`renderOption`),menuPropsRef:k(e,`menuProps`),doSelect:C,doUpdateShow:w}),E(a,t=>{!e.animated&&!t&&D()});function C(t,n){let{onSelect:r}=e;r&&V(r,t,n)}function w(t){let{"onUpdate:show":n,onUpdateShow:r}=e;n&&V(n,t),r&&V(r,t),i.value=t}function D(){c.value=null,u.value=null,d.value=null}function O(){w(!1)}function j(){L(`left`)}function M(){L(`right`)}function N(){L(`up`)}function P(){L(`down`)}function F(){let e=I();e?.isLeaf&&a.value&&(C(e.key,e.rawNode),w(!1))}function I(){let{value:e}=o,{value:t}=f;return!e||t===null?null:e.getNode(t)??null}function L(e){let{value:t}=f,{value:{getFirstAvailableNode:n}}=o,r=null;if(t===null){let e=n();e!==null&&(r=e.key)}else{let t=I();if(t){let n;switch(e){case`down`:n=t.getNext();break;case`up`:n=t.getPrev();break;case`right`:n=t.getChild();break;case`left`:n=t.getParent()}n&&(r=n.key)}}r!==null&&(c.value=null,u.value=r)}let R=A(()=>{let{inverted:n}=e,r=b.value,{common:{cubicBezierEaseInOut:i},self:a}=S.value,{padding:o,dividerColor:s,borderRadius:c,optionOpacityDisabled:l,[t(`optionIconSuffixWidth`,r)]:u,[t(`optionSuffixWidth`,r)]:d,[t(`optionIconPrefixWidth`,r)]:f,[t(`optionPrefixWidth`,r)]:p,[t(`fontSize`,r)]:m,[t(`optionHeight`,r)]:h,[t(`optionIconSize`,r)]:g}=a,_={"--n-bezier":i,"--n-font-size":m,"--n-padding":o,"--n-border-radius":c,"--n-option-height":h,"--n-option-prefix-width":p,"--n-option-icon-prefix-width":f,"--n-option-suffix-width":d,"--n-option-icon-suffix-width":u,"--n-option-icon-size":g,"--n-divider-color":s,"--n-option-opacity-disabled":l};return n?(_[`--n-color`]=a.colorInverted,_[`--n-option-color-hover`]=a.optionColorHoverInverted,_[`--n-option-color-active`]=a.optionColorActiveInverted,_[`--n-option-text-color`]=a.optionTextColorInverted,_[`--n-option-text-color-hover`]=a.optionTextColorHoverInverted,_[`--n-option-text-color-active`]=a.optionTextColorActiveInverted,_[`--n-option-text-color-child-active`]=a.optionTextColorChildActiveInverted,_[`--n-prefix-color`]=a.prefixColorInverted,_[`--n-suffix-color`]=a.suffixColorInverted,_[`--n-group-header-text-color`]=a.groupHeaderTextColorInverted):(_[`--n-color`]=a.color,_[`--n-option-color-hover`]=a.optionColorHover,_[`--n-option-color-active`]=a.optionColorActive,_[`--n-option-text-color`]=a.optionTextColor,_[`--n-option-text-color-hover`]=a.optionTextColorHover,_[`--n-option-text-color-active`]=a.optionTextColorActive,_[`--n-option-text-color-child-active`]=a.optionTextColorChildActive,_[`--n-prefix-color`]=a.prefixColor,_[`--n-suffix-color`]=a.suffixColor,_[`--n-group-header-text-color`]=a.groupHeaderTextColor),_}),z=v?r(`dropdown`,A(()=>`${b.value[0]}${e.inverted?`i`:``}`),R,e):void 0;return{mergedClsPrefix:_,mergedTheme:S,mergedSize:b,tmNodes:s,mergedShow:a,handleAfterLeave:()=>{e.animated&&D()},doUpdateShow:w,cssVars:v?void 0:R,themeClass:z?.themeClass,onRender:z?.onRender}},render(){let e=(e,t,n,r,i)=>{let{mergedClsPrefix:a,menuProps:o}=this;this.onRender?.();let s=o?.(void 0,this.tmNodes.map(e=>e.rawNode))||{},c={ref:de(t),class:[e,`${a}-dropdown`,`${a}-dropdown--${this.mergedSize}-size`,this.themeClass],clsPrefix:a,tmNodes:this.tmNodes,style:[...n,this.cssVars],showArrow:this.showArrow,arrowStyle:this.arrowStyle,scrollable:this.scrollable,onMouseenter:r,onMouseleave:i};return D($,g(this.$attrs,c,s))},{mergedTheme:t}=this,n={show:this.mergedShow,theme:t.peers.Popover,themeOverrides:t.peerOverrides.Popover,internalOnAfterLeave:this.handleAfterLeave,internalRenderBody:e,onUpdateShow:this.doUpdateShow,"onUpdate:show":void 0};return f(),_(W,z(this.$props,we,n),{_:1,trigger:O(()=>this.$slots.default?.())},16)}});export{he as n,Ee as t};