import{$t as e,A as t,Bt as n,Cn as r,En as i,F as a,Ft as o,G as s,K as c,Kt as l,Lt as u,M as d,Mn as f,P as p,Pn as m,Pt as h,Qn as g,Rt as _,Sn as v,Tn as y,Un as b,Y as x,Yt as S,Z as C,_ as w,_n as T,an as E,dn as D,et as ee,g as O,hn as k,in as A,j,jn as M,k as N,pn as P,q as F,qn as I,rn as L,sn as R,un as te,vn as z,xn as ne,yn as B,zt as V}from"./light-TBGKFU-e.js";import{A as H,B as U,E as re,H as ie,L as W,N as G,at as K,b as q,k as J,ot as Y,p as ae,q as X,rt as oe,st as se,w as ce,y as Z}from"./request-BS4zoC18.js";import{t as le}from"./use-locale-C3uQHj1W.js";import{c as ue,d as de,f as fe,l as pe,n as me,o as Q,r as he,s as ge,t as _e,u as ve}from"./create-G_I1UjLa.js";import{t as ye}from"./next-frame-once-qdYFoq8G.js";import{t as $}from"./happens-in-D4nrdV77.js";import{t as be}from"./Empty-BDxvvZdG.js";import{r as xe,s as Se,t as Ce}from"./light-XL89zeHF.js";import{n as we,t as Te}from"./cssr-BSqWL002.js";import{t as Ee}from"./use-merged-state-Cqr2carY.js";import{t as De}from"./use-compitable-CpPvjT_b.js";import{t as Oe}from"./Tag-C-cXGVZa.js";import{n as ke}from"./Input-CEDJiX-T.js";function Ae(e){return e&-e}var je=class{constructor(e,t){this.l=e,this.min=t;let n=Array(e+1);for(let t=0;t<e+1;++t)n[t]=0;this.ft=n}add(e,t){if(t===0)return;let{l:n,ft:r}=this;for(e+=1;e<=n;)r[e]+=t,e+=Ae(e)}get(e){return this.sum(e+1)-this.sum(e)}sum(e){if(e===void 0&&(e=this.l),e<=0)return 0;let{ft:t,min:n,l:r}=this;if(e>r)throw Error("[FinweckTree.sum]: `i` is larger than length.");let i=e*n;for(;e>0;)i+=t[e],e-=Ae(e);return i}getBound(e){let t=0,n=this.l;for(;n>t;){let r=Math.floor((t+n)/2),i=this.sum(r);if(i>e){n=r;continue}if(i<e){if(t===r)return this.sum(t+1)<=e?t+1:r;t=r}else return r}return t}},Me;function Ne(){return typeof document>`u`?!1:(Me===void 0&&(Me=`matchMedia`in window&&window.matchMedia(`(pointer:coarse)`).matches),Me)}var Pe;function Fe(){return typeof document>`u`?1:(Pe===void 0&&(Pe=`chrome`in window?window.devicePixelRatio:1),Pe)}var Ie=`VVirtualListXScroll`;function Le({columnsRef:e,renderColRef:t,renderItemWithColsRef:n}){let r=b(0),a=b(0),o=L(()=>{let t=e.value;if(t.length===0)return null;let n=new je(t.length,0);return t.forEach((e,t)=>{n.add(t,e.width)}),n}),s=j(()=>{let e=o.value;return e===null?0:Math.max(e.getBound(a.value)-1,0)}),c=e=>{let t=o.value;return t===null?0:t.sum(e)},l=j(()=>{let t=o.value;return t===null?0:Math.min(t.getBound(a.value+r.value)+1,e.value.length-1)});return i(Ie,{startIndexRef:s,endIndexRef:l,columnsRef:e,renderColRef:t,renderItemWithColsRef:n,getLeft:c}),{listWidthRef:r,scrollLeftRef:a}}var Re=D({name:`VirtualListRow`,props:{index:{type:Number,required:!0},item:{type:Object,required:!0}},setup(){let{startIndexRef:e,endIndexRef:t,columnsRef:n,getLeft:r,renderColRef:i,renderItemWithColsRef:a}=k(Ie);return{startIndex:e,endIndex:t,columns:n,renderCol:i,renderItemWithCols:a,getLeft:r}},render(){let{startIndex:e,endIndex:t,columns:n,renderCol:r,renderItemWithCols:i,getLeft:a,item:o}=this;if(i!=null)return i({itemIndex:this.index,startColIndex:e,endColIndex:t,allColumns:n,item:o,getLeft:a});if(r!=null){let i=[];for(let s=e;s<=t;++s){let e=n[s];i.push(r({column:e,left:a(s),item:o}))}return i}return null}}),ze=Te(`.v-vl`,{maxHeight:`inherit`,height:`100%`,overflow:`auto`,minWidth:`1px`},[Te(`&:not(.v-vl--show-scrollbar)`,{scrollbarWidth:`none`},[Te(`&::-webkit-scrollbar, &::-webkit-scrollbar-track-piece, &::-webkit-scrollbar-thumb`,{width:0,height:0,display:`none`})])]),Be=D({name:`VirtualList`,inheritAttrs:!1,props:{showScrollbar:{type:Boolean,default:!0},columns:{type:Array,default:()=>[]},renderCol:Function,renderItemWithCols:Function,items:{type:Array,default:()=>[]},itemSize:{type:Number,required:!0},itemResizable:Boolean,itemsStyle:[String,Object],visibleItemsTag:{type:[String,Object],default:`div`},visibleItemsProps:Object,ignoreItemResize:Boolean,onScroll:Function,onWheel:Function,onResize:Function,defaultScrollKey:[Number,String],defaultScrollIndex:Number,keyField:{type:String,default:`key`},paddingTop:{type:[Number,String],default:0},paddingBottom:{type:[Number,String],default:0}},setup(e){let t=C();ze.mount({id:`vueuc/virtual-list`,head:!0,anchorMetaName:we,ssr:t}),r(()=>{let{defaultScrollIndex:t,defaultScrollKey:n}=e;t==null?n!=null&&_({key:n}):_({index:t})});let n=!1,i=!1;B(()=>{if(n=!1,!i){i=!0;return}_({top:m.value,left:s.value})}),v(()=>{n=!0,i||=!0});let a=j(()=>{if(e.renderCol==null&&e.renderItemWithCols==null||e.columns.length===0)return;let t=0;return e.columns.forEach(e=>{t+=e.width}),t}),o=L(()=>{let t=new Map,{keyField:n}=e;return e.items.forEach((e,r)=>{t.set(e[n],r)}),t}),{scrollLeftRef:s,listWidthRef:c}=Le({columnsRef:I(e,`columns`),renderColRef:I(e,`renderCol`),renderItemWithColsRef:I(e,`renderItemWithCols`)}),l=b(null),u=b(void 0),d=new Map,f=L(()=>{let{items:t,itemSize:n,keyField:r}=e,i=new je(t.length,n);return t.forEach((e,t)=>{let n=e[r],a=d.get(n);a!==void 0&&i.add(t,a)}),i}),p=b(0),m=b(0),h=j(()=>Math.max(f.value.getBound(m.value-oe(e.paddingTop))-1,0)),g=L(()=>{let{value:t}=u;if(t===void 0)return[];let{items:n,itemSize:r}=e,i=h.value,a=Math.min(i+Math.ceil(t/r+1),n.length-1),o=[];for(let e=i;e<=a;++e)o.push(n[e]);return o}),_=(e,t)=>{if(typeof e==`number`){w(e,t,`auto`);return}let{left:n,top:r,index:i,key:a,position:s,behavior:c,debounce:l=!0}=e;if(n!==void 0||r!==void 0)w(n,r,c);else if(i!==void 0)S(i,c,l);else if(a!==void 0){let e=o.value.get(a);e!==void 0&&S(e,c,l)}else s===`bottom`?w(0,2**53-1,c):s===`top`&&w(0,0,c)},y,x=null;function S(t,n,r){let i=l.value;if(i==null)return;let{value:a}=f,o=a.sum(t)+oe(e.paddingTop);if(!r)i.scrollTo({left:0,top:o,behavior:n});else{y=t,x!==null&&window.clearTimeout(x),x=window.setTimeout(()=>{y=void 0,x=null},16);let{scrollTop:e,offsetHeight:r}=i;if(o>e){let s=a.get(t);o+s<=e+r||i.scrollTo({left:0,top:o+s-r,behavior:n})}else i.scrollTo({left:0,top:o,behavior:n})}}function w(e,t,n){l.value?.scrollTo({left:e,top:t,behavior:n})}function T(t,r){if(n||e.ignoreItemResize||M(r.target))return;let{value:i}=f,a=o.value.get(t),s=i.get(a),c=r.borderBoxSize?.[0]?.blockSize??r.contentRect.height;if(c===s)return;c-e.itemSize===0?d.delete(t):d.set(t,c-e.itemSize);let u=c-s;if(u===0)return;i.add(a,u);let m=l.value;if(m!=null){if(y===void 0){let e=i.sum(a);m.scrollTop>e&&m.scrollBy(0,u)}else(a<y||a===y&&c+i.sum(a)>m.scrollTop+m.offsetHeight)&&m.scrollBy(0,u);A()}p.value++}let E=!Ne(),D=!1;function ee(t){var n;(n=e.onScroll)==null||n.call(e,t),(!E||!D)&&A()}function O(t){var n;if((n=e.onWheel)==null||n.call(e,t),E){let e=l.value;if(e!=null){if(t.deltaX===0&&(e.scrollTop===0&&t.deltaY<=0||e.scrollTop+e.offsetHeight>=e.scrollHeight&&t.deltaY>=0))return;t.preventDefault(),e.scrollTop+=t.deltaY/Fe(),e.scrollLeft+=t.deltaX/Fe(),A(),D=!0,ye(()=>{D=!1})}}}function k(t){if(n||M(t.target))return;if(e.renderCol==null&&e.renderItemWithCols==null){if(t.contentRect.height===u.value)return}else if(t.contentRect.height===u.value&&t.contentRect.width===c.value)return;u.value=t.contentRect.height,c.value=t.contentRect.width;let{onResize:r}=e;r!==void 0&&r(t)}function A(){let{value:e}=l;e!=null&&(m.value=e.scrollTop,s.value=e.scrollLeft)}function M(e){let t=e;for(;t!==null;){if(t.style.display===`none`)return!0;t=t.parentElement}return!1}return{listHeight:u,listStyle:{overflow:`auto`},keyToIndex:o,itemsStyle:L(()=>{let{itemResizable:t}=e,n=Y(f.value.sum());return p.value,[e.itemsStyle,{boxSizing:`content-box`,width:Y(a.value),height:t?``:n,minHeight:t?n:``,paddingTop:Y(e.paddingTop),paddingBottom:Y(e.paddingBottom)}]}),visibleItemsStyle:L(()=>(p.value,{transform:`translateY(${Y(f.value.sum(h.value))})`})),viewportItems:g,listElRef:l,itemsElRef:b(null),scrollTo:_,handleListResize:k,handleListScroll:ee,handleListWheel:O,handleItemResize:T}},render(){let{itemResizable:e,keyField:t,keyToIndex:n,visibleItemsTag:r}=this;return P(J,{onResize:this.handleListResize},{default:()=>{var i;return P(`div`,T(this.$attrs,{class:[`v-vl`,this.showScrollbar&&`v-vl--show-scrollbar`],onScroll:this.handleListScroll,onWheel:this.handleListWheel,ref:`listElRef`}),[this.items.length===0?(i=this.$slots).empty?.call(i):P(`div`,{ref:`itemsElRef`,class:`v-vl-items`,style:this.itemsStyle},[P(r,Object.assign({class:`v-vl-visible-items`,style:this.visibleItemsStyle},this.visibleItemsProps),{default:()=>{let{renderCol:r,renderItemWithCols:i}=this;return this.viewportItems.map(a=>{let o=a[t],s=n.get(o),c=r==null?void 0:P(Re,{index:s,item:a}),l=i==null?void 0:P(Re,{index:s,item:a}),u=this.$slots.default({item:a,renderedCols:c,renderedItemWithCols:l,index:s})[0];return e?P(J,{key:o,onResize:e=>this.handleItemResize(o,e)},{default:()=>u}):(u.key=o,u)})}})])])}})}});function Ve(e){switch(typeof e){case`string`:return e||void 0;case`number`:return String(e);default:return}}function He(e,t){t&&(r(()=>{let{value:n}=e;n&&H.registerHandler(n,t)}),M(e,(e,t)=>{t&&H.unregisterHandler(t)},{deep:!1}),ne(()=>{let{value:t}=e;t&&H.unregisterHandler(t)}))}var Ue=D({props:{onFocus:Function,onBlur:Function},setup(e){return()=>(()=>{let t=s(`d16ead82505dc285`);return y(),R(`div`,{style:`width: 0; height: 0`,tabindex:0,onFocus:t[0]||=t=>e.onFocus?.(t),onBlur:t[1]||=t=>e.onBlur?.(t)},null,32)})()}}),We=D({name:`NBaseSelectGroupHeader`,props:{clsPrefix:{type:String,required:!0},tmNode:{type:Object,required:!0}},setup(){let{renderLabelRef:e,renderOptionRef:t,labelFieldRef:n,nodePropsRef:r}=k(fe);return{labelField:n,nodeProps:r,renderLabel:e,renderOption:t}},render(){let{clsPrefix:e,renderLabel:t,renderOption:n,nodeProps:r,tmNode:{rawNode:i}}=this,a=r?.(i),o=t?t(i,!1):O(i[this.labelField],i,!1),s=(y(),R(`div`,T(a,{class:[`${e}-base-select-group-header`,a?.class]}),[x(()=>o)],16));return i.render?i.render({node:s,option:i}):n?n({node:s,option:i,selected:!1}):s}}),Ge=D({name:`Checkmark`,render(){return(()=>{let e=s(`3c84eac8ae4e1f96`);return e[0]||=A(`svg`,{xmlns:`http://www.w3.org/2000/svg`,viewBox:`0 0 16 16`},[A(`g`,{fill:`none`},[A(`path`,{d:`M14.046 3.486a.75.75 0 0 1-.032 1.06l-7.93 7.474a.85.85 0 0 1-1.188-.022l-2.68-2.72a.75.75 0 1 1 1.068-1.053l2.234 2.267l7.468-7.038a.75.75 0 0 1 1.06.032z`,fill:`currentColor`})])],-1)})()}}),Ke=[`onClick`,`onMouseenter`,`onMousemove`];function qe(e,t){return y(),E(l,{name:`fade-in-scale-up-transition`},{default:()=>e?(y(),E(d,{key:1,clsPrefix:t,class:c(`${t}-base-select-option__check`)},{default:()=>P(Ge)},1032,[`clsPrefix`,`class`])):null},1024)}var Je=D({name:`NBaseSelectOption`,props:{clsPrefix:{type:String,required:!0},tmNode:{type:Object,required:!0}},setup(e){let{valueRef:t,pendingTmNodeRef:n,multipleRef:r,valueSetRef:i,renderLabelRef:a,renderOptionRef:o,labelFieldRef:s,valueFieldRef:c,showCheckmarkRef:l,nodePropsRef:u,handleOptionClick:d,handleOptionMouseEnter:f}=k(fe),p=j(()=>{let{value:t}=n;return t?e.tmNode.key===t.key:!1});function m(t){let{tmNode:n}=e;n.disabled||d(t,n)}function h(t){let{tmNode:n}=e;n.disabled||f(t,n)}function g(t){let{tmNode:n}=e,{value:r}=p;n.disabled||r||f(t,n)}return{multiple:r,isGrouped:j(()=>{let{tmNode:t}=e,{parent:n}=t;return n&&n.rawNode.type===`group`}),showCheckmark:l,nodeProps:u,isPending:p,isSelected:j(()=>{let{value:n}=t,{value:a}=r;if(n===null)return!1;let o=e.tmNode.rawNode[c.value];if(a){let{value:e}=i;return e.has(o)}return n===o}),labelField:s,renderLabel:a,renderOption:o,handleMouseMove:g,handleMouseEnter:h,handleClick:m}},render(){let{clsPrefix:e,tmNode:{rawNode:t},isSelected:n,isPending:r,isGrouped:i,showCheckmark:a,nodeProps:o,renderOption:s,renderLabel:l,handleClick:u,handleMouseEnter:d,handleMouseMove:f}=this,p=qe(n,e),m=l?[l(t,n),a&&p]:[O(t[this.labelField],t,n),a&&p],h=o?.(t),g=(y(),R(`div`,T(h,{class:[`${e}-base-select-option`,t.class,h?.class,{[`${e}-base-select-option--disabled`]:t.disabled,[`${e}-base-select-option--selected`]:n,[`${e}-base-select-option--grouped`]:i,[`${e}-base-select-option--pending`]:r,[`${e}-base-select-option--show-checkmark`]:a}],style:[h?.style||``,t.style||``],onClick:q([u,h?.onClick]),onMouseenter:q([d,h?.onMouseenter]),onMousemove:q([f,h?.onMousemove])}),[A(`div`,{class:c(`${e}-base-select-option__content`)},[x(()=>m)],2)],16,Ke));return t.render?t.render({node:g,option:t,selected:n}):s?s({node:g,option:t,selected:n}):g}}),Ye=o(`base-select-menu`,`
 line-height: 1.5;
 outline: none;
 z-index: 0;
 position: relative;
 border-radius: var(--n-border-radius);
 transition:
 background-color .3s var(--n-bezier),
 box-shadow .3s var(--n-bezier);
 background-color: var(--n-color);
`,[o(`scrollbar`,`
 max-height: var(--n-height);
 `),o(`virtual-list`,`
 max-height: var(--n-height);
 `),o(`base-select-option`,`
 min-height: var(--n-option-height);
 font-size: var(--n-option-font-size);
 display: flex;
 align-items: center;
 `,[u(`content`,`
 z-index: 1;
 white-space: nowrap;
 text-overflow: ellipsis;
 overflow: hidden;
 `)]),o(`base-select-group-header`,`
 min-height: var(--n-option-height);
 font-size: .93em;
 display: flex;
 align-items: center;
 `),o(`base-select-menu-option-wrapper`,`
 position: relative;
 width: 100%;
 `),u(`loading, empty`,`
 display: flex;
 padding: 12px 32px;
 flex: 1;
 justify-content: center;
 `),u(`loading`,`
 color: var(--n-loading-color);
 font-size: var(--n-loading-size);
 `),u(`header`,`
 padding: 8px var(--n-option-padding-left);
 font-size: var(--n-option-font-size);
 transition: 
 color .3s var(--n-bezier),
 border-color .3s var(--n-bezier);
 border-bottom: 1px solid var(--n-action-divider-color);
 color: var(--n-action-text-color);
 `),u(`action`,`
 padding: 8px var(--n-option-padding-left);
 font-size: var(--n-option-font-size);
 transition: 
 color .3s var(--n-bezier),
 border-color .3s var(--n-bezier);
 border-top: 1px solid var(--n-action-divider-color);
 color: var(--n-action-text-color);
 `),o(`base-select-group-header`,`
 position: relative;
 cursor: default;
 padding: var(--n-option-padding);
 color: var(--n-group-header-text-color);
 `),o(`base-select-option`,`
 cursor: pointer;
 position: relative;
 padding: var(--n-option-padding);
 transition:
 color .3s var(--n-bezier),
 opacity .3s var(--n-bezier);
 box-sizing: border-box;
 color: var(--n-option-text-color);
 opacity: 1;
 `,[_(`show-checkmark`,`
 padding-right: calc(var(--n-option-padding-right) + 20px);
 `),h(`&::before`,`
 content: "";
 position: absolute;
 left: 4px;
 right: 4px;
 top: 0;
 bottom: 0;
 border-radius: var(--n-border-radius);
 transition: background-color .3s var(--n-bezier);
 `),h(`&:active`,`
 color: var(--n-option-text-color-pressed);
 `),_(`grouped`,`
 padding-left: calc(var(--n-option-padding-left) * 1.5);
 `),_(`pending`,[h(`&::before`,`
 background-color: var(--n-option-color-pending);
 `)]),_(`selected`,`
 color: var(--n-option-text-color-active);
 `,[h(`&::before`,`
 background-color: var(--n-option-color-active);
 `),_(`pending`,[h(`&::before`,`
 background-color: var(--n-option-color-active-pending);
 `)])]),_(`disabled`,`
 cursor: not-allowed;
 `,[V(`selected`,`
 color: var(--n-option-text-color-disabled);
 `),_(`selected`,`
 opacity: var(--n-option-opacity-disabled);
 `)]),u(`check`,`
 font-size: 16px;
 position: absolute;
 right: calc(var(--n-option-padding-right) - 4px);
 top: calc(50% - 7px);
 color: var(--n-option-check-color);
 transition: color .3s var(--n-bezier);
 `,[Z({enterScale:`0.5`})])])]),Xe=[`tabindex`,`onFocusin`,`onFocusout`,`onKeyup`,`onKeydown`,`onMousedown`,`onMouseenter`,`onMouseleave`],Ze=D({name:`InternalSelectMenu`,props:{...p.props,clsPrefix:{type:String,required:!0},scrollable:{type:Boolean,default:!0},treeMate:{type:Object,required:!0},multiple:Boolean,size:{type:String,default:`medium`},value:{type:[String,Number,Array],default:null},autoPending:Boolean,virtualScroll:{type:Boolean,default:!0},show:{type:Boolean,default:!0},labelField:{type:String,default:`label`},valueField:{type:String,default:`value`},loading:Boolean,focusable:Boolean,renderLabel:Function,renderOption:Function,nodeProps:Function,showCheckmark:{type:Boolean,default:!0},onMousedown:Function,onScroll:Function,onFocus:Function,onBlur:Function,onKeyup:Function,onKeydown:Function,onTabOut:Function,onMouseenter:Function,onMouseleave:Function,onResize:Function,resetMenuOnOptionsChange:{type:Boolean,default:!0},inlineThemeDisabled:Boolean,scrollbarProps:Object,onToggle:Function},setup(e){let{mergedClsPrefixRef:t,mergedRtlRef:o,mergedComponentPropsRef:s}=ee(e),c=N(`InternalSelectMenu`,o,t),l=p(`InternalSelectMenu`,`-internal-select-menu`,Ye,Se,e,I(e,`clsPrefix`)),u=b(null),d=b(null),f=b(null),m=L(()=>e.treeMate.getFlattenedNodes()),h=L(()=>me(m.value)),g=b(null);function _(){let{treeMate:t}=e,n=null,{value:r}=e;r===null?n=t.getFirstAvailableNode():(n=e.multiple?t.getNode((r||[])[(r||[]).length-1]):t.getNode(r),(!n||n.disabled)&&(n=t.getFirstAvailableNode())),H(n||null)}function v(){let{value:t}=g;t&&!e.treeMate.getNode(t.key)&&(g.value=null)}let y;M(()=>e.show,t=>{t?y=M(()=>e.treeMate,()=>{e.resetMenuOnOptionsChange?(e.autoPending?_():v(),z(U)):v()},{immediate:!0}):y?.()},{immediate:!0}),ne(()=>{y?.()});let x=L(()=>oe(l.value.self[n(`optionHeight`,e.size)])),S=L(()=>K(l.value.self[n(`padding`,e.size)])),C=L(()=>e.multiple&&Array.isArray(e.value)?new Set(e.value):new Set),w=L(()=>{let e=m.value;return e&&e.length===0}),T=L(()=>s?.value?.Select?.renderEmpty);function E(t){let{onToggle:n}=e;n&&n(t)}function D(t){let{onScroll:n}=e;n&&n(t)}function O(e){f.value?.sync(),D(e)}function k(){f.value?.sync()}function A(){let{value:e}=g;return e||null}function j(e,t){t.disabled||H(t,!1)}function P(e,t){t.disabled||E(t)}function F(t){$(t,`action`)||e.onKeyup?.(t)}function R(t){$(t,`action`)||e.onKeydown?.(t)}function te(t){e.onMousedown?.(t),!e.focusable&&t.preventDefault()}function B(){let{value:e}=g;e&&H(e.getNext({loop:!0}),!0)}function V(){let{value:e}=g;e&&H(e.getPrev({loop:!0}),!0)}function H(e,t=!1){g.value=e,t&&U()}function U(){let t=g.value;if(!t)return;let n=h.value(t.key);n!==null&&(e.virtualScroll?d.value?.scrollTo({index:n}):f.value?.scrollTo({index:n,elSize:x.value}))}function re(t){u.value?.contains(t.target)&&e.onFocus?.(t)}function ie(t){u.value?.contains(t.relatedTarget)||e.onBlur?.(t)}i(fe,{handleOptionMouseEnter:j,handleOptionClick:P,valueSetRef:C,pendingTmNodeRef:g,nodePropsRef:I(e,`nodeProps`),showCheckmarkRef:I(e,`showCheckmark`),multipleRef:I(e,`multiple`),valueRef:I(e,`value`),renderLabelRef:I(e,`renderLabel`),renderOptionRef:I(e,`renderOption`),labelFieldRef:I(e,`labelField`),valueFieldRef:I(e,`valueField`)}),i(de,u),r(()=>{let{value:e}=f;e&&e.sync()});let W=L(()=>{let{size:t}=e,{common:{cubicBezierEaseInOut:r},self:{height:i,borderRadius:a,color:o,groupHeaderTextColor:s,actionDividerColor:c,optionTextColorPressed:u,optionTextColor:d,optionTextColorDisabled:f,optionTextColorActive:p,optionOpacityDisabled:m,optionCheckColor:h,actionTextColor:g,optionColorPending:_,optionColorActive:v,loadingColor:y,loadingSize:b,optionColorActivePending:x,[n(`optionFontSize`,t)]:S,[n(`optionHeight`,t)]:C,[n(`optionPadding`,t)]:w}}=l.value;return{"--n-height":i,"--n-action-divider-color":c,"--n-action-text-color":g,"--n-bezier":r,"--n-border-radius":a,"--n-color":o,"--n-option-font-size":S,"--n-group-header-text-color":s,"--n-option-check-color":h,"--n-option-color-pending":_,"--n-option-color-active":v,"--n-option-color-active-pending":x,"--n-option-height":C,"--n-option-opacity-disabled":m,"--n-option-text-color":d,"--n-option-text-color-active":p,"--n-option-text-color-disabled":f,"--n-option-text-color-pressed":u,"--n-option-padding":w,"--n-option-padding-left":K(w,`left`),"--n-option-padding-right":K(w,`right`),"--n-loading-color":y,"--n-loading-size":b}}),{inlineThemeDisabled:G}=e,q=G?a(`internal-select-menu`,L(()=>e.size[0]),W,e):void 0,J={selfRef:u,next:B,prev:V,getPendingTmNode:A};return He(u,e.onResize),{mergedTheme:l,mergedClsPrefix:t,rtlEnabled:c,virtualListRef:d,scrollbarRef:f,itemSize:x,padding:S,flattenedNodes:m,empty:w,mergedRenderEmpty:T,virtualListContainer(){let{value:e}=d;return e?.listElRef},virtualListContent(){let{value:e}=d;return e?.itemsElRef},doScroll:D,handleFocusin:re,handleFocusout:ie,handleKeyUp:F,handleKeyDown:R,handleMouseDown:te,handleVirtualListResize:k,handleVirtualListScroll:O,cssVars:G?void 0:W,themeClass:q?.themeClass,onRender:q?.onRender,...J}},render(){let{$slots:t,virtualScroll:n,clsPrefix:r,mergedTheme:i,themeClass:a,onRender:o}=this;return o?.(),y(),R(`div`,{ref:`selfRef`,tabindex:this.focusable?0:-1,class:c([`${r}-base-select-menu`,`${r}-base-select-menu--${this.size}-size`,this.rtlEnabled&&`${r}-base-select-menu--rtl`,a,this.multiple&&`${r}-base-select-menu--multiple`]),style:g(this.cssVars),onFocusin:this.handleFocusin,onFocusout:this.handleFocusout,onKeyup:this.handleKeyUp,onKeydown:this.handleKeyDown,onMousedown:this.handleMouseDown,onMouseenter:this.onMouseenter,onMouseleave:this.onMouseleave},[x(()=>ie(t.header,e=>e&&(y(),R(`div`,{class:c(`${r}-base-select-menu__header`),"data-header":!0,key:`header`},[x(()=>e)],2)))),this.loading?(y(),R(`div`,{key:0,class:c(`${r}-base-select-menu__loading`)},[(y(),E(w,{clsPrefix:r,strokeWidth:20},null,8,[`clsPrefix`]))],2)):(y(),R(e,{key:1},[this.empty?(y(),R(`div`,{key:1,class:c(`${r}-base-select-menu__empty`),"data-empty":!0},[x(()=>U(t.empty,()=>[this.mergedRenderEmpty?.()||(y(),E(be,{theme:i.peers.Empty,themeOverrides:i.peerOverrides.Empty,size:this.size},null,8,[`theme`,`themeOverrides`,`size`]))]))],2)):(y(),E(re,T({key:0,ref:`scrollbarRef`,theme:i.peers.Scrollbar,themeOverrides:i.peerOverrides.Scrollbar,scrollable:this.scrollable,container:n?this.virtualListContainer:void 0,content:n?this.virtualListContent:void 0,onScroll:n?void 0:this.doScroll},this.scrollbarProps),{default:()=>n?(y(),E(Be,{key:1,ref:`virtualListRef`,class:c(`${r}-virtual-list`),items:this.flattenedNodes,itemSize:this.itemSize,showScrollbar:!1,paddingTop:this.padding.top,paddingBottom:this.padding.bottom,onResize:this.handleVirtualListResize,onScroll:this.handleVirtualListScroll,itemResizable:!0},{default:({item:e})=>e.isGroup?(y(),E(We,{key:e.key,clsPrefix:r,tmNode:e},null,8,[`clsPrefix`,`tmNode`])):e.ignored?null:(y(),E(Je,{clsPrefix:r,key:e.key,tmNode:e},null,8,[`clsPrefix`,`tmNode`]))},1032,[`class`,`items`,`itemSize`,`paddingTop`,`paddingBottom`,`onResize`,`onScroll`])):(y(),R(`div`,{key:4,class:c(`${r}-base-select-menu-option-wrapper`),style:g({paddingTop:this.padding.top,paddingBottom:this.padding.bottom})},[x(()=>this.flattenedNodes.map(e=>e.isGroup?(y(),E(We,{key:e.key,clsPrefix:r,tmNode:e},null,8,[`clsPrefix`,`tmNode`])):(y(),E(Je,{clsPrefix:r,key:e.key,tmNode:e},null,8,[`clsPrefix`,`tmNode`]))))],6))},1040,[`theme`,`themeOverrides`,`scrollable`,`container`,`content`,`onScroll`]))],64)),x(()=>ie(t.action,e=>e&&[(y(),R(`div`,{class:c(`${r}-base-select-menu__action`),"data-action":!0,key:`action`},[x(()=>e)],2)),(y(),E(Ue,{onFocus:this.onTabOut,key:`focus-detector`},null,8,[`onFocus`]))]))],46,Xe)}});function Qe(e){return e.type===`group`}function $e(e){return e.type===`ignored`}function et(e,t){try{return!!(1+t.toString().toLowerCase().indexOf(e.trim().toLowerCase()))}catch{return!1}}function tt(e,t){return{getIsGroup:Qe,getIgnored:$e,getKey(t){return Qe(t)?t.name||t.key||`key-required`:t[e]},getChildren(e){return e[t]}}}function nt(e,t,n,r){if(!t)return e;function i(e){if(!Array.isArray(e))return[];let a=[];for(let o of e)if(Qe(o)){let e=i(o[r]);e.length&&a.push(Object.assign({},o,{[r]:e}))}else if($e(o))continue;else t(n,o)&&a.push(o);return a}return i(e)}function rt(e,t,n){let r=new Map;return e.forEach(e=>{Qe(e)?e[n].forEach(e=>{r.set(e[t],e)}):r.set(e[t],e)}),r}var it=h([o(`base-selection`,`
 --n-padding-single: var(--n-padding-single-top) var(--n-padding-single-right) var(--n-padding-single-bottom) var(--n-padding-single-left);
 --n-padding-multiple: var(--n-padding-multiple-top) var(--n-padding-multiple-right) var(--n-padding-multiple-bottom) var(--n-padding-multiple-left);
 position: relative;
 z-index: auto;
 box-shadow: none;
 width: 100%;
 max-width: 100%;
 display: inline-block;
 vertical-align: bottom;
 border-radius: var(--n-border-radius);
 min-height: var(--n-height);
 line-height: 1.5;
 font-size: var(--n-font-size);
 `,[o(`base-loading`,`
 color: var(--n-loading-color);
 `),o(`base-selection-tags`,`min-height: var(--n-height);`),u(`border, state-border`,`
 position: absolute;
 left: 0;
 right: 0;
 top: 0;
 bottom: 0;
 pointer-events: none;
 border: var(--n-border);
 border-radius: inherit;
 transition:
 box-shadow .3s var(--n-bezier),
 border-color .3s var(--n-bezier);
 `),u(`state-border`,`
 z-index: 1;
 border-color: #0000;
 `),o(`base-suffix`,`
 cursor: pointer;
 position: absolute;
 top: 50%;
 transform: translateY(-50%);
 right: 10px;
 `,[u(`arrow`,`
 font-size: var(--n-arrow-size);
 color: var(--n-arrow-color);
 transition: color .3s var(--n-bezier);
 `)]),o(`base-selection-overlay`,`
 display: flex;
 align-items: center;
 white-space: nowrap;
 pointer-events: none;
 position: absolute;
 top: 0;
 right: 0;
 bottom: 0;
 left: 0;
 padding: var(--n-padding-single);
 transition: color .3s var(--n-bezier);
 `,[u(`wrapper`,`
 flex-basis: 0;
 flex-grow: 1;
 overflow: hidden;
 text-overflow: ellipsis;
 `)]),o(`base-selection-placeholder`,`
 color: var(--n-placeholder-color);
 `,[u(`inner`,`
 max-width: 100%;
 overflow: hidden;
 `)]),o(`base-selection-tags`,`
 cursor: pointer;
 outline: none;
 box-sizing: border-box;
 position: relative;
 z-index: auto;
 display: flex;
 padding: var(--n-padding-multiple);
 flex-wrap: wrap;
 align-items: center;
 width: 100%;
 vertical-align: bottom;
 background-color: var(--n-color);
 border-radius: inherit;
 transition:
 color .3s var(--n-bezier),
 box-shadow .3s var(--n-bezier),
 background-color .3s var(--n-bezier);
 `),o(`base-selection-label`,`
 height: var(--n-height);
 display: inline-flex;
 width: 100%;
 vertical-align: bottom;
 cursor: pointer;
 outline: none;
 z-index: auto;
 box-sizing: border-box;
 position: relative;
 transition:
 color .3s var(--n-bezier),
 box-shadow .3s var(--n-bezier),
 background-color .3s var(--n-bezier);
 border-radius: inherit;
 background-color: var(--n-color);
 align-items: center;
 `,[o(`base-selection-input`,`
 font-size: inherit;
 line-height: inherit;
 outline: none;
 cursor: pointer;
 box-sizing: border-box;
 border:none;
 width: 100%;
 padding: var(--n-padding-single);
 background-color: #0000;
 color: var(--n-text-color);
 transition: color .3s var(--n-bezier);
 caret-color: var(--n-caret-color);
 `,[u(`content`,`
 text-overflow: ellipsis;
 overflow: hidden;
 white-space: nowrap; 
 `)]),u(`render-label`,`
 color: var(--n-text-color);
 `)]),V(`disabled`,[h(`&:hover`,[u(`state-border`,`
 box-shadow: var(--n-box-shadow-hover);
 border: var(--n-border-hover);
 `)]),_(`focus`,[u(`state-border`,`
 box-shadow: var(--n-box-shadow-focus);
 border: var(--n-border-focus);
 `)]),_(`active`,[u(`state-border`,`
 box-shadow: var(--n-box-shadow-active);
 border: var(--n-border-active);
 `),o(`base-selection-label`,`background-color: var(--n-color-active);`),o(`base-selection-tags`,`background-color: var(--n-color-active);`)])]),_(`disabled`,`cursor: not-allowed;`,[u(`arrow`,`
 color: var(--n-arrow-color-disabled);
 `),o(`base-selection-label`,`
 cursor: not-allowed;
 background-color: var(--n-color-disabled);
 `,[o(`base-selection-input`,`
 cursor: not-allowed;
 color: var(--n-text-color-disabled);
 `),u(`render-label`,`
 color: var(--n-text-color-disabled);
 `)]),o(`base-selection-tags`,`
 cursor: not-allowed;
 background-color: var(--n-color-disabled);
 `),o(`base-selection-placeholder`,`
 cursor: not-allowed;
 color: var(--n-placeholder-color-disabled);
 `)]),o(`base-selection-input-tag`,`
 height: calc(var(--n-height) - 6px);
 line-height: calc(var(--n-height) - 6px);
 outline: none;
 display: none;
 position: relative;
 margin-bottom: 3px;
 max-width: 100%;
 vertical-align: bottom;
 `,[u(`input`,`
 font-size: inherit;
 font-family: inherit;
 min-width: 1px;
 padding: 0;
 background-color: #0000;
 outline: none;
 border: none;
 max-width: 100%;
 overflow: hidden;
 width: 1em;
 line-height: inherit;
 cursor: pointer;
 color: var(--n-text-color);
 caret-color: var(--n-caret-color);
 `),u(`mirror`,`
 position: absolute;
 left: 0;
 top: 0;
 white-space: pre;
 visibility: hidden;
 user-select: none;
 -webkit-user-select: none;
 opacity: 0;
 `)]),[`warning`,`error`].map(e=>_(`${e}-status`,[u(`state-border`,`border: var(--n-border-${e});`),V(`disabled`,[h(`&:hover`,[u(`state-border`,`
 box-shadow: var(--n-box-shadow-hover-${e});
 border: var(--n-border-hover-${e});
 `)]),_(`active`,[u(`state-border`,`
 box-shadow: var(--n-box-shadow-active-${e});
 border: var(--n-border-active-${e});
 `),o(`base-selection-label`,`background-color: var(--n-color-active-${e});`),o(`base-selection-tags`,`background-color: var(--n-color-active-${e});`)]),_(`focus`,[u(`state-border`,`
 box-shadow: var(--n-box-shadow-focus-${e});
 border: var(--n-border-focus-${e});
 `)])])]))]),o(`base-selection-popover`,`
 margin-bottom: -3px;
 display: flex;
 flex-wrap: wrap;
 margin-right: -8px;
 `),o(`base-selection-tag-wrapper`,`
 max-width: 100%;
 display: inline-flex;
 padding: 0 7px 3px 0;
 `,[h(`&:last-child`,`padding-right: 0;`),o(`tag`,`
 font-size: 14px;
 max-width: 100%;
 `,[u(`content`,`
 line-height: 1.25;
 text-overflow: ellipsis;
 overflow: hidden;
 `)])])]),at=[`disabled`,`value`,`autofocus`,`onBlur`,`onFocus`,`onKeydown`,`onInput`,`onCompositionstart`,`onCompositionend`],ot=[`tabindex`],st=[`title`],ct=[`value`,`readonly`,`disabled`,`autofocus`,`onFocus`,`onBlur`,`onInput`,`onCompositionstart`,`onCompositionend`],lt=[`tabindex`],ut=[`onClick`,`onMouseenter`,`onMouseleave`,`onKeydown`,`onFocusin`,`onFocusout`,`onMousedown`],dt=D({name:`InternalSelection`,props:{...p.props,clsPrefix:{type:String,required:!0},bordered:{type:Boolean,default:void 0},active:Boolean,pattern:{type:String,default:``},placeholder:String,selectedOption:{type:Object,default:null},selectedOptions:{type:Array,default:null},labelField:{type:String,default:`label`},valueField:{type:String,default:`value`},multiple:Boolean,filterable:Boolean,clearable:Boolean,disabled:Boolean,size:{type:String,default:`medium`},loading:Boolean,autofocus:Boolean,showArrow:{type:Boolean,default:!0},inputProps:Object,focused:Boolean,renderTag:Function,onKeydown:Function,onClick:Function,onBlur:Function,onFocus:Function,onDeleteOption:Function,maxTagCount:[String,Number],ellipsisTagPopoverProps:Object,onClear:Function,onPatternInput:Function,onPatternFocus:Function,onPatternBlur:Function,renderLabel:Function,status:String,inlineThemeDisabled:Boolean,ignoreComposition:{type:Boolean,default:!0},onResize:Function},setup(e){let{mergedClsPrefixRef:t,mergedRtlRef:i}=ee(e),o=N(`InternalSelection`,i,t),s=b(null),c=b(null),l=b(null),u=b(null),d=b(null),m=b(null),h=b(null),g=b(null),_=b(null),v=b(null),y=b(!1),x=b(!1),S=b(!1),C=p(`InternalSelection`,`-internal-selection`,it,xe,e,I(e,`clsPrefix`)),w=L(()=>e.clearable&&!e.disabled&&(S.value||e.active)),T=L(()=>e.selectedOption?e.renderTag?e.renderTag({option:e.selectedOption,handleClose:()=>{}}):e.renderLabel?e.renderLabel(e.selectedOption,!0):O(e.selectedOption[e.labelField],e.selectedOption,!0):e.placeholder),E=L(()=>{let t=e.selectedOption;if(t)return t[e.labelField]}),D=L(()=>e.multiple?!!(Array.isArray(e.selectedOptions)&&e.selectedOptions.length):e.selectedOption!==null);function k(){let{value:t}=s;if(t){let{value:n}=c;n&&(n.style.width=`${t.offsetWidth}px`,e.maxTagCount!==`responsive`&&_.value?.sync({showAllItemsBeforeCalculate:!1}))}}function A(){let{value:e}=v;e&&(e.style.display=`none`)}function j(){let{value:e}=v;e&&(e.style.display=`inline-block`)}M(I(e,`active`),e=>{e||A()}),M(I(e,`pattern`),()=>{e.multiple&&z(k)});function P(t){let{onFocus:n}=e;n&&n(t)}function F(t){let{onBlur:n}=e;n&&n(t)}function R(t){let{onDeleteOption:n}=e;n&&n(t)}function te(t){let{onClear:n}=e;n&&n(t)}function ne(t){let{onPatternInput:n}=e;n&&n(t)}function B(e){(!e.relatedTarget||!l.value?.contains(e.relatedTarget))&&P(e)}function V(e){l.value?.contains(e.relatedTarget)||F(e)}function H(e){te(e)}function U(){S.value=!0}function re(){S.value=!1}function ie(t){e.active&&e.filterable&&t.target!==c.value&&t.preventDefault()}function W(e){R(e)}let G=b(!1);function q(t){if(t.key===`Backspace`&&!G.value&&!e.pattern.length){let{selectedOptions:t}=e;t?.length&&W(t[t.length-1])}}let J=null;function Y(t){let{value:n}=s;n&&(n.textContent=t.target.value,k()),e.ignoreComposition&&G.value?J=t:ne(t)}function ae(){G.value=!0}function X(){G.value=!1,e.ignoreComposition&&ne(J),J=null}function oe(t){x.value=!0,e.onPatternFocus?.(t)}function se(t){x.value=!1,e.onPatternBlur?.(t)}function ce(){if(e.filterable)x.value=!1,m.value?.blur(),c.value?.blur();else if(e.multiple){let{value:e}=u;e?.blur()}else{let{value:e}=d;e?.blur()}}function Z(){e.filterable?(x.value=!1,m.value?.focus()):e.multiple?u.value?.focus():d.value?.focus()}function le(){let{value:e}=c;e&&(j(),e.focus())}function ue(){let{value:e}=c;e&&e.blur()}function de(e){let{value:t}=h;t&&t.setTextContent(`+${e}`)}function fe(){let{value:e}=g;return e}function pe(){return c.value}let me=null;function Q(){me!==null&&window.clearTimeout(me)}function he(){e.active||(Q(),me=window.setTimeout(()=>{D.value&&(y.value=!0)},100))}function ge(){Q()}function _e(e){e||(Q(),y.value=!1)}M(D,e=>{e||(y.value=!1)}),r(()=>{f(()=>{let t=m.value;t&&(e.disabled?t.removeAttribute(`tabindex`):t.tabIndex=x.value?-1:0)})}),He(l,e.onResize);let{inlineThemeDisabled:ve}=e,ye=L(()=>{let{size:t}=e,{common:{cubicBezierEaseInOut:r},self:{fontWeight:i,borderRadius:a,color:o,placeholderColor:s,textColor:c,paddingSingle:l,paddingMultiple:u,caretColor:d,colorDisabled:f,textColorDisabled:p,placeholderColorDisabled:m,colorActive:h,boxShadowFocus:g,boxShadowActive:_,boxShadowHover:v,border:y,borderFocus:b,borderHover:x,borderActive:S,arrowColor:w,arrowColorDisabled:T,loadingColor:E,colorActiveWarning:D,boxShadowFocusWarning:ee,boxShadowActiveWarning:O,boxShadowHoverWarning:k,borderWarning:A,borderFocusWarning:j,borderHoverWarning:M,borderActiveWarning:N,colorActiveError:P,boxShadowFocusError:F,boxShadowActiveError:I,boxShadowHoverError:L,borderError:R,borderFocusError:te,borderHoverError:z,borderActiveError:ne,clearColor:B,clearColorHover:V,clearColorPressed:H,clearSize:U,arrowSize:re,[n(`height`,t)]:ie,[n(`fontSize`,t)]:W}}=C.value,G=K(l),q=K(u);return{"--n-bezier":r,"--n-border":y,"--n-border-active":S,"--n-border-focus":b,"--n-border-hover":x,"--n-border-radius":a,"--n-box-shadow-active":_,"--n-box-shadow-focus":g,"--n-box-shadow-hover":v,"--n-caret-color":d,"--n-color":o,"--n-color-active":h,"--n-color-disabled":f,"--n-font-size":W,"--n-height":ie,"--n-padding-single-top":G.top,"--n-padding-multiple-top":q.top,"--n-padding-single-right":G.right,"--n-padding-multiple-right":q.right,"--n-padding-single-left":G.left,"--n-padding-multiple-left":q.left,"--n-padding-single-bottom":G.bottom,"--n-padding-multiple-bottom":q.bottom,"--n-placeholder-color":s,"--n-placeholder-color-disabled":m,"--n-text-color":c,"--n-text-color-disabled":p,"--n-arrow-color":w,"--n-arrow-color-disabled":T,"--n-loading-color":E,"--n-color-active-warning":D,"--n-box-shadow-focus-warning":ee,"--n-box-shadow-active-warning":O,"--n-box-shadow-hover-warning":k,"--n-border-warning":A,"--n-border-focus-warning":j,"--n-border-hover-warning":M,"--n-border-active-warning":N,"--n-color-active-error":P,"--n-box-shadow-focus-error":F,"--n-box-shadow-active-error":I,"--n-box-shadow-hover-error":L,"--n-border-error":R,"--n-border-focus-error":te,"--n-border-hover-error":z,"--n-border-active-error":ne,"--n-clear-size":U,"--n-clear-color":B,"--n-clear-color-hover":V,"--n-clear-color-pressed":H,"--n-arrow-size":re,"--n-font-weight":i}}),$=ve?a(`internal-selection`,L(()=>e.size[0]),ye,e):void 0;return{mergedTheme:C,mergedClearable:w,mergedClsPrefix:t,rtlEnabled:o,patternInputFocused:x,filterablePlaceholder:T,label:E,selected:D,showTagsPanel:y,isComposing:G,counterRef:h,counterWrapperRef:g,patternInputMirrorRef:s,patternInputRef:c,selfRef:l,multipleElRef:u,singleElRef:d,patternInputWrapperRef:m,overflowRef:_,inputTagElRef:v,handleMouseDown:ie,handleFocusin:B,handleClear:H,handleMouseEnter:U,handleMouseLeave:re,handleDeleteOption:W,handlePatternKeyDown:q,handlePatternInputInput:Y,handlePatternInputBlur:se,handlePatternInputFocus:oe,handleMouseEnterCounter:he,handleMouseLeaveCounter:ge,handleFocusout:V,handleCompositionEnd:X,handleCompositionStart:ae,onPopoverUpdateShow:_e,focus:Z,focusInput:le,blur:ce,blurInput:ue,updateCounter:de,getCounter:fe,getTail:pe,renderLabel:e.renderLabel,cssVars:ve?void 0:ye,themeClass:$?.themeClass,onRender:$?.onRender}},render(){let{status:t,multiple:n,size:r,disabled:i,filterable:a,maxTagCount:o,bordered:s,clsPrefix:l,ellipsisTagPopoverProps:u,onRender:d,renderTag:f,renderLabel:p}=this;d?.();let m=o===`responsive`,h=typeof o==`number`,_=m||h,v=(y(),E(W,null,{default:()=>(y(),E(ke,{clsPrefix:l,loading:this.loading,showArrow:this.showArrow,showClear:this.mergedClearable&&this.selected,onClear:this.handleClear},{default:()=>this.$slots.arrow?.()},1032,[`clsPrefix`,`loading`,`showArrow`,`showClear`,`onClear`]))},1024)),b;if(n){let{labelField:t}=this,n=n=>(y(),R(`div`,{class:c(`${l}-base-selection-tag-wrapper`),key:n.value},[f?(y(),R(e,{key:0},[x(()=>f({option:n,handleClose:()=>{this.handleDeleteOption(n)}}))],64)):(y(),E(Oe,{key:1,size:r,closable:!n.disabled,disabled:i,onClose:()=>{this.handleDeleteOption(n)},internalCloseIsButtonTag:!1,internalCloseFocusable:!1},{default:()=>p?p(n,!0):O(n[t],n,!0)},1032,[`size`,`closable`,`disabled`,`onClose`]))],2)),s=()=>(h?this.selectedOptions.slice(0,o):this.selectedOptions).map(n),d=a?(y(),R(`div`,{class:c(`${l}-base-selection-input-tag`),ref:`inputTagElRef`,key:`__input-tag__`},[A(`input`,T(this.inputProps,{ref:`patternInputRef`,tabindex:-1,disabled:i,value:this.pattern,autofocus:this.autofocus,class:`${l}-base-selection-input-tag__input`,onBlur:this.handlePatternInputBlur,onFocus:this.handlePatternInputFocus,onKeydown:this.handlePatternKeyDown,onInput:this.handlePatternInputInput,onCompositionstart:this.handleCompositionStart,onCompositionend:this.handleCompositionEnd}),null,16,at),A(`span`,{ref:`patternInputMirrorRef`,class:c(`${l}-base-selection-input-tag__mirror`)},[x(()=>this.pattern)],2)],2)):null,g=m?()=>(y(),R(`div`,{class:c(`${l}-base-selection-tag-wrapper`),ref:`counterWrapperRef`},[(y(),E(Oe,{size:r,ref:`counterRef`,onMouseenter:this.handleMouseEnterCounter,onMouseleave:this.handleMouseLeaveCounter,disabled:i},null,8,[`size`,`onMouseenter`,`onMouseleave`,`disabled`]))],2)):void 0,S;if(h){let e=this.selectedOptions.length-o;e>0&&(S=(t=>(y(),R(`div`,{class:c(`${l}-base-selection-tag-wrapper`),key:`__counter__`},[(y(),E(Oe,{size:r,ref:`counterRef`,onMouseenter:this.handleMouseEnterCounter,disabled:i},{default:()=>`+${e}`},1032,[`size`,`onMouseenter`,`disabled`]))],2)))(S))}let C=m?a?(y(),E(Q,{key:3,ref:`overflowRef`,updateCounter:this.updateCounter,getCounter:this.getCounter,getTail:this.getTail,style:{width:`100%`,display:`flex`,overflow:`hidden`}},{default:s,counter:g,tail:()=>d},1032,[`updateCounter`,`getCounter`,`getTail`])):(y(),E(Q,{key:4,ref:`overflowRef`,updateCounter:this.updateCounter,getCounter:this.getCounter,style:{width:`100%`,display:`flex`,overflow:`hidden`}},{default:s,counter:g},1032,[`updateCounter`,`getCounter`])):h&&S?s().concat(S):s(),w=_?()=>(y(),R(`div`,{class:c(`${l}-base-selection-popover`)},[m?(y(),R(e,{key:0},[x(()=>s())],64)):(y(),R(e,{key:1},[x(()=>this.selectedOptions.map(n))],64))],2)):void 0,D=_?{show:this.showTagsPanel,trigger:`hover`,overlap:!0,placement:`top`,width:`trigger`,onUpdateShow:this.onPopoverUpdateShow,theme:this.mergedTheme.peers.Popover,themeOverrides:this.mergedTheme.peerOverrides.Popover,...u}:null,ee=!this.selected&&(!this.active||!this.pattern&&!this.isComposing)?(y(),R(`div`,{key:5,class:c(`${l}-base-selection-placeholder ${l}-base-selection-overlay`)},[A(`div`,{class:c(`${l}-base-selection-placeholder__inner`)},[x(()=>this.placeholder)],2)],2)):null,k=a?(y(),R(`div`,{key:6,ref:`patternInputWrapperRef`,class:c(`${l}-base-selection-tags`)},[x(()=>C),m?x(()=>null):(y(),R(e,{key:1},[x(()=>d)],64)),x(()=>v)],2)):(y(),R(`div`,{key:7,ref:`multipleElRef`,class:c(`${l}-base-selection-tags`),tabindex:i?void 0:0},[x(()=>C),x(()=>v)],10,ot));b=(t=>(y(),R(e,{key:8},[_?(y(),E(he,T({key:0},D,{scrollable:!0,style:`max-height: calc(var(--v-target-height) * 6.6);`}),{trigger:()=>k,default:w},1040)):(y(),R(e,{key:1},[x(()=>k)],64)),x(()=>ee)],64)))(b)}else if(a){let t=this.pattern||this.isComposing,n=this.active?!t:!this.selected,r=!this.active&&this.selected;b=(t=>(y(),R(`div`,{key:9,ref:`patternInputWrapperRef`,class:c(`${l}-base-selection-label`),title:this.patternInputFocused?void 0:Ve(this.label)},[A(`input`,T(this.inputProps,{ref:`patternInputRef`,class:`${l}-base-selection-input`,value:this.active?this.pattern:``,placeholder:``,readonly:i,disabled:i,tabindex:-1,autofocus:this.autofocus,onFocus:this.handlePatternInputFocus,onBlur:this.handlePatternInputBlur,onInput:this.handlePatternInputInput,onCompositionstart:this.handleCompositionStart,onCompositionend:this.handleCompositionEnd}),null,16,ct),r?(y(),R(`div`,{class:c(`${l}-base-selection-label__render-label ${l}-base-selection-overlay`),key:`input`},[A(`div`,{class:c(`${l}-base-selection-overlay__wrapper`)},[f?(y(),R(e,{key:0},[x(()=>f({option:this.selectedOption,handleClose:()=>{}}))],64)):(y(),R(e,{key:1},[p?(y(),R(e,{key:0},[x(()=>p(this.selectedOption,!0))],64)):(y(),R(e,{key:1},[x(()=>O(this.label,this.selectedOption,!0))],64))],64))],2)],2)):x(()=>null),n?(y(),R(`div`,{class:c(`${l}-base-selection-placeholder ${l}-base-selection-overlay`),key:`placeholder`},[A(`div`,{class:c(`${l}-base-selection-overlay__wrapper`)},[x(()=>this.filterablePlaceholder)],2)],2)):x(()=>null),x(()=>v)],10,st)))(b)}else b=(t=>(y(),R(`div`,{key:10,ref:`singleElRef`,class:c(`${l}-base-selection-label`),tabindex:this.disabled?void 0:0},[this.label===void 0?(y(),R(`div`,{class:c(`${l}-base-selection-placeholder ${l}-base-selection-overlay`),key:`placeholder`},[A(`div`,{class:c(`${l}-base-selection-placeholder__inner`)},[x(()=>this.placeholder)],2)],2)):(y(),R(`div`,{class:c(`${l}-base-selection-input`),title:Ve(this.label),key:`input`},[A(`div`,{class:c(`${l}-base-selection-input__content`)},[f?(y(),R(e,{key:0},[x(()=>f({option:this.selectedOption,handleClose:()=>{}}))],64)):(y(),R(e,{key:1},[p?(y(),R(e,{key:0},[x(()=>p(this.selectedOption,!0))],64)):(y(),R(e,{key:1},[x(()=>O(this.label,this.selectedOption,!0))],64))],64))],2)],10,[`title`])),x(()=>v)],10,lt)))(b);return y(),R(`div`,{ref:`selfRef`,class:c([`${l}-base-selection`,this.rtlEnabled&&`${l}-base-selection--rtl`,this.themeClass,t&&`${l}-base-selection--${t}-status`,{[`${l}-base-selection--active`]:this.active,[`${l}-base-selection--selected`]:this.selected||this.active&&this.pattern,[`${l}-base-selection--disabled`]:this.disabled,[`${l}-base-selection--multiple`]:this.multiple,[`${l}-base-selection--focus`]:this.focused}]),style:g(this.cssVars),onClick:this.onClick,onMouseenter:this.handleMouseEnter,onMouseleave:this.handleMouseLeave,onKeydown:this.onKeydown,onFocusin:this.handleFocusin,onFocusout:this.handleFocusout,onMousedown:this.handleMouseDown},[x(()=>b),s?(y(),R(`div`,{key:0,class:c(`${l}-base-selection__border`)},null,2)):x(()=>null),s?(y(),R(`div`,{key:2,class:c(`${l}-base-selection__state-border`)},null,2)):x(()=>null)],46,ut)}}),ft=h([o(`select`,`
 z-index: auto;
 outline: none;
 width: 100%;
 position: relative;
 font-weight: var(--n-font-weight);
 `),o(`select-menu`,`
 margin: 4px 0;
 box-shadow: var(--n-menu-box-shadow);
 `,[Z({originalTransition:`background-color .3s var(--n-bezier), box-shadow .3s var(--n-bezier)`})])]),pt={...p.props,to:ve.propTo,bordered:{type:Boolean,default:void 0},clearable:Boolean,clearCreatedOptionsOnClear:{type:Boolean,default:!0},clearFilterAfterSelect:{type:Boolean,default:!0},options:{type:Array,default:()=>[]},defaultValue:{type:[String,Number,Array],default:null},keyboard:{type:Boolean,default:!0},value:[String,Number,Array],placeholder:String,menuProps:Object,multiple:Boolean,size:String,menuSize:{type:String},filterable:Boolean,disabled:{type:Boolean,default:void 0},remote:Boolean,loading:Boolean,filter:Function,placement:{type:String,default:`bottom-start`},widthMode:{type:String,default:`trigger`},tag:Boolean,onCreate:Function,fallbackOption:{type:[Function,Boolean],default:void 0},show:{type:Boolean,default:void 0},showArrow:{type:Boolean,default:!0},maxTagCount:[Number,String],ellipsisTagPopoverProps:Object,consistentMenuWidth:{type:Boolean,default:!0},virtualScroll:{type:Boolean,default:!0},labelField:{type:String,default:`label`},valueField:{type:String,default:`value`},childrenField:{type:String,default:`children`},renderLabel:Function,renderOption:Function,renderTag:Function,"onUpdate:value":[Function,Array],inputProps:Object,nodeProps:Function,ignoreComposition:{type:Boolean,default:!0},showOnFocus:Boolean,onUpdateValue:[Function,Array],onBlur:[Function,Array],onClear:[Function,Array],onFocus:[Function,Array],onScroll:[Function,Array],onSearch:[Function,Array],onUpdateShow:[Function,Array],"onUpdate:show":[Function,Array],displayDirective:{type:String,default:`show`},resetMenuOnOptionsChange:{type:Boolean,default:!0},status:String,showCheckmark:{type:Boolean,default:!0},scrollbarProps:Object,onChange:[Function,Array],items:Array},mt=D({name:`Select`,props:pt,slots:Object,setup(e){let{mergedClsPrefixRef:n,mergedBorderedRef:r,namespaceRef:i,inlineThemeDisabled:o,mergedComponentPropsRef:s}=ee(e),c=p(`Select`,`-select`,ft,Ce,e,n),l=b(e.defaultValue),u=I(e,`value`),d=Ee(u,l),f=b(!1),m=b(``),h=De(e,[`items`,`options`]),g=b([]),_=b([]),v=L(()=>_.value.concat(g.value).concat(h.value)),y=L(()=>{let{filter:t}=e;if(t)return t;let{labelField:n,valueField:r}=e;return(e,t)=>{if(!t)return!1;let i=t[n];if(typeof i==`string`)return et(e,i);let a=t[r];return typeof a==`string`?et(e,a):typeof a==`number`&&et(e,String(a))}}),x=L(()=>{if(e.remote)return h.value;{let{value:t}=v,{value:n}=m;return!n.length||!e.filterable?t:nt(t,y.value,n,e.childrenField)}}),S=L(()=>{let{valueField:t,childrenField:n}=e,r=tt(t,n);return _e(x.value,r)}),C=L(()=>rt(v.value,e.valueField,e.childrenField)),w=b(!1),T=Ee(I(e,`show`),w),E=b(null),D=b(null),O=b(null),{localeRef:k}=le(`Select`),A=L(()=>e.placeholder??k.value.placeholder),j=[],N=b(new Map),P=L(()=>{let{fallbackOption:t}=e;if(t===void 0){let{labelField:t,valueField:n}=e;return e=>({[t]:String(e),[n]:e})}return t===!1?!1:e=>Object.assign(t(e),{value:e})});function F(t){let n=e.remote,{value:r}=N,{value:i}=C,{value:a}=P,o=[];return t.forEach(e=>{if(i.has(e))o.push(i.get(e));else if(n&&r.has(e))o.push(r.get(e));else if(a){let t=a(e);t&&o.push(t)}}),o}let R=L(()=>{if(e.multiple){let{value:e}=d;return Array.isArray(e)?F(e):[]}return null}),te=L(()=>{let{value:t}=d;return!e.multiple&&!Array.isArray(t)?t===null?null:F([t])[0]||null:null}),z=ce(e,{mergedSize:t=>{let{size:n}=e;if(n)return n;let{mergedSize:r}=t||{};return r?.value?r.value:s?.value?.Select?.size||`medium`}}),{mergedSizeRef:ne,mergedDisabledRef:B,mergedStatusRef:V}=z;function H(t,n){let{onChange:r,"onUpdate:value":i,onUpdateValue:a}=e,{nTriggerFormChange:o,nTriggerFormInput:s}=z;r&&X(r,t,n),a&&X(a,t,n),i&&X(i,t,n),l.value=t,o(),s()}function U(t){let{onBlur:n}=e,{nTriggerFormBlur:r}=z;n&&X(n,t),r()}function re(){let{onClear:t}=e;t&&X(t)}function ie(t){let{onFocus:n,showOnFocus:r}=e,{nTriggerFormFocus:i}=z;n&&X(n,t),i(),r&&J()}function W(t){let{onSearch:n}=e;n&&X(n,t)}function G(t){let{onScroll:n}=e;n&&X(n,t)}function K(){let{remote:t,multiple:n}=e;if(t){let{value:t}=N;if(n){let{valueField:n}=e;R.value?.forEach(e=>{t.set(e[n],e)})}else{let n=te.value;n&&t.set(n[e.valueField],n)}}}function q(t){let{onUpdateShow:n,"onUpdate:show":r}=e;n&&X(n,t),r&&X(r,t),w.value=t}function J(){B.value||(q(!0),w.value=!0,e.filterable&&Ne())}function Y(){q(!1)}function oe(){m.value=``,_.value=j}let Z=b(!1);function ue(){e.filterable&&(Z.value=!0)}function de(){e.filterable&&(Z.value=!1,T.value||oe())}function fe(){B.value||(T.value?e.filterable?Ne():Y():J())}function pe(e){O.value?.selfRef?.contains(e.relatedTarget)||(f.value=!1,U(e),Y())}function me(e){ie(e),f.value=!0}function Q(){f.value=!0}function he(e){E.value?.$el.contains(e.relatedTarget)||(f.value=!1,U(e),Y())}function ge(){E.value?.focus(),Y()}function ye(e){T.value&&(E.value?.$el.contains(se(e))||Y())}function be(t){if(!Array.isArray(t))return[];if(P.value)return Array.from(t);{let{remote:n}=e,{value:r}=C;if(n){let{value:e}=N;return t.filter(t=>r.has(t)||e.has(t))}return t.filter(e=>r.has(e))}}function xe(e){Se(e.rawNode)}function Se(t){if(B.value)return;let{tag:n,remote:r,clearFilterAfterSelect:i,valueField:a}=e;if(n&&!r){let{value:e}=_,t=e[0]||null;if(t){let e=g.value;e.length?e.push(t):g.value=[t],_.value=j}}if(r&&N.value.set(t[a],t),e.multiple){let e=be(d.value),o=e.findIndex(e=>e===t[a]);if(~o){if(e.splice(o,1),n&&!r){let e=we(t[a]);~e&&(g.value.splice(e,1),i&&(m.value=``))}}else e.push(t[a]),i&&(m.value=``);H(e,F(e))}else{if(n&&!r){let e=we(t[a]);~e?g.value=[g.value[e]]:g.value=j}Me(),Y(),H(t[a],t)}}function we(t){return g.value.findIndex(n=>n[e.valueField]===t)}function Te(t){T.value||J();let{value:n}=t.target;m.value=n;let{tag:r,remote:i}=e;if(W(n),r&&!i){if(!n){_.value=j;return}let{onCreate:t}=e,r=t?t(n):{[e.labelField]:n,[e.valueField]:n},{valueField:i,labelField:a}=e;h.value.some(e=>e[i]===r[i]||e[a]===r[a])||g.value.some(e=>e[i]===r[i]||e[a]===r[a])?_.value=j:_.value=[r]}}function Oe(t){t.stopPropagation();let{multiple:n,tag:r,remote:i,clearCreatedOptionsOnClear:a}=e;!n&&e.filterable&&Y(),r&&!i&&a&&(g.value=j),re(),n?H([],[]):H(null,null)}function ke(e){!$(e,`action`)&&!$(e,`empty`)&&!$(e,`header`)&&e.preventDefault()}function Ae(e){G(e)}function je(t){if(!e.keyboard){t.preventDefault();return}switch(t.key){case` `:if(e.filterable)break;t.preventDefault();case`Enter`:if(!E.value?.isComposing){if(T.value){let t=O.value?.getPendingTmNode();t?xe(t):e.filterable||(Y(),Me())}else if(J(),e.tag&&Z.value){let t=_.value[0];if(t){let n=t[e.valueField],{value:r}=d;e.multiple&&Array.isArray(r)&&r.includes(n)||Se(t)}}}t.preventDefault();break;case`ArrowUp`:if(t.preventDefault(),e.loading)return;T.value&&O.value?.prev();break;case`ArrowDown`:if(t.preventDefault(),e.loading)return;T.value?O.value?.next():J();break;case`Escape`:T.value&&(ae(t),Y()),E.value?.focus()}}function Me(){E.value?.focus()}function Ne(){E.value?.focusInput()}function Pe(){T.value&&D.value?.syncPosition()}K(),M(I(e,`options`),K);let Fe={focus:()=>{E.value?.focus()},focusInput:()=>{E.value?.focusInput()},blur:()=>{E.value?.blur()},blurInput:()=>{E.value?.blurInput()}},Ie=L(()=>{let{self:{menuBoxShadow:e}}=c.value;return{"--n-menu-box-shadow":e}}),Le=o?a(`select`,void 0,Ie,e):void 0;return{...Fe,mergedStatus:V,mergedClsPrefix:n,mergedBordered:r,namespace:i,treeMate:S,isMounted:t(),triggerRef:E,menuRef:O,pattern:m,uncontrolledShow:w,mergedShow:T,adjustedTo:ve(e),uncontrolledValue:l,mergedValue:d,followerRef:D,localizedPlaceholder:A,selectedOption:te,selectedOptions:R,mergedSize:ne,mergedDisabled:B,focused:f,activeWithoutMenuOpen:Z,inlineThemeDisabled:o,onTriggerInputFocus:ue,onTriggerInputBlur:de,handleTriggerOrMenuResize:Pe,handleMenuFocus:Q,handleMenuBlur:he,handleMenuTabOut:ge,handleTriggerClick:fe,handleToggle:xe,handleDeleteOption:Se,handlePatternInput:Te,handleClear:Oe,handleTriggerBlur:pe,handleTriggerFocus:me,handleKeydown:je,handleMenuAfterLeave:oe,handleMenuClickOutside:ye,handleMenuScroll:Ae,handleMenuKeydown:je,handleMenuMousedown:ke,mergedTheme:c,cssVars:o?void 0:Ie,themeClass:Le?.themeClass,onRender:Le?.onRender}},render(){return y(),R(`div`,{class:c(`${this.mergedClsPrefix}-select`)},[te(pe,null,{_:1,default:F(()=>[(y(),E(ue,null,{_:1,default:F(()=>(y(),E(dt,{ref:`triggerRef`,inlineThemeDisabled:this.inlineThemeDisabled,status:this.mergedStatus,inputProps:this.inputProps,clsPrefix:this.mergedClsPrefix,showArrow:this.showArrow,maxTagCount:this.maxTagCount,ellipsisTagPopoverProps:this.ellipsisTagPopoverProps,bordered:this.mergedBordered,active:this.activeWithoutMenuOpen||this.mergedShow,pattern:this.pattern,placeholder:this.localizedPlaceholder,selectedOption:this.selectedOption,selectedOptions:this.selectedOptions,multiple:this.multiple,renderTag:this.renderTag,renderLabel:this.renderLabel,filterable:this.filterable,clearable:this.clearable,disabled:this.mergedDisabled,size:this.mergedSize,theme:this.mergedTheme.peers.InternalSelection,labelField:this.labelField,valueField:this.valueField,themeOverrides:this.mergedTheme.peerOverrides.InternalSelection,loading:this.loading,focused:this.focused,onClick:this.handleTriggerClick,onDeleteOption:this.handleDeleteOption,onPatternInput:this.handlePatternInput,onClear:this.handleClear,onBlur:this.handleTriggerBlur,onFocus:this.handleTriggerFocus,onKeydown:this.handleKeydown,onPatternBlur:this.onTriggerInputBlur,onPatternFocus:this.onTriggerInputFocus,onResize:this.handleTriggerOrMenuResize,ignoreComposition:this.ignoreComposition},{_:1,arrow:F(()=>[this.$slots.arrow?.()])},8,`inlineThemeDisabled.status.inputProps.clsPrefix.showArrow.maxTagCount.ellipsisTagPopoverProps.bordered.active.pattern.placeholder.selectedOption.selectedOptions.multiple.renderTag.renderLabel.filterable.clearable.disabled.size.theme.labelField.valueField.themeOverrides.loading.focused.onClick.onDeleteOption.onPatternInput.onClear.onBlur.onFocus.onKeydown.onPatternBlur.onPatternFocus.onResize.ignoreComposition`.split(`.`))))})),(y(),E(ge,{ref:`followerRef`,show:this.mergedShow,to:this.adjustedTo,teleportDisabled:this.adjustedTo===ve.tdkey,containerClass:this.namespace,width:this.consistentMenuWidth?`target`:void 0,minWidth:`target`,placement:this.placement},{_:1,default:F(()=>(y(),E(l,{name:`fade-in-scale-up-transition`,appear:this.isMounted,onAfterLeave:this.handleMenuAfterLeave},{_:1,default:F(()=>this.mergedShow||this.displayDirective===`show`?(this.onRender?.(),m((y(),E(Ze,T(this.menuProps,{ref:`menuRef`,onResize:this.handleTriggerOrMenuResize,inlineThemeDisabled:this.inlineThemeDisabled,virtualScroll:this.consistentMenuWidth&&this.virtualScroll,class:[`${this.mergedClsPrefix}-select-menu`,this.themeClass,this.menuProps?.class],clsPrefix:this.mergedClsPrefix,focusable:!0,labelField:this.labelField,valueField:this.valueField,autoPending:!0,nodeProps:this.nodeProps,theme:this.mergedTheme.peers.InternalSelectMenu,themeOverrides:this.mergedTheme.peerOverrides.InternalSelectMenu,treeMate:this.treeMate,multiple:this.multiple,size:this.menuSize,renderOption:this.renderOption,renderLabel:this.renderLabel,value:this.mergedValue,style:[this.menuProps?.style,this.cssVars],onToggle:this.handleToggle,onScroll:this.handleMenuScroll,onFocus:this.handleMenuFocus,onBlur:this.handleMenuBlur,onKeydown:this.handleMenuKeydown,onTabOut:this.handleMenuTabOut,onMousedown:this.handleMenuMousedown,show:this.mergedShow,showCheckmark:this.showCheckmark,resetMenuOnOptionsChange:this.resetMenuOnOptionsChange,scrollbarProps:this.scrollbarProps}),{_:1,empty:F(()=>[this.$slots.empty?.()]),header:F(()=>[this.$slots.header?.()]),action:F(()=>[this.$slots.action?.()])},16,`onResize.inlineThemeDisabled.virtualScroll.class.clsPrefix.labelField.valueField.nodeProps.theme.themeOverrides.treeMate.multiple.size.renderOption.renderLabel.value.style.onToggle.onScroll.onFocus.onBlur.onKeydown.onTabOut.onMousedown.show.showCheckmark.resetMenuOnOptionsChange.scrollbarProps`.split(`.`))),this.displayDirective===`show`?[[S,this.mergedShow],[G,this.handleMenuClickOutside,void 0,{capture:!0}]]:[[G,this.handleMenuClickOutside,void 0,{capture:!0}]])):null)},8,[`appear`,`onAfterLeave`])))},8,[`show`,`to`,`teleportDisabled`,`containerClass`,`width`,`placement`]))])})],2)}});export{Be as i,tt as n,Ze as r,mt as t};