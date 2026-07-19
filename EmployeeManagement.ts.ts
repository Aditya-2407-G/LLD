/*

Employee Access Management - grant / revoke / get

The implementation is broken down into parts where we have done incremental updates to the previous parts.

// Part 1:  initial implementation

type Permission = 'read' | 'write' | 'admin';

interface GrantMetadata {
    grantedAt: number;
    grantedBy: string;
}

interface GrantRecord {
    permission: Permission;
    metadata: GrantMetadata;
}


class AccessManager {
    
// emp-> resource-> grants
private accessStore: Map<string, Map<string, GrantRecord[]>> = new Map();

public grant(empId: string, resId: string, permission: Permission, grantedBy: string): void {
    
if(!this.accessStore.get(empId)) {
    this.accessStore.set(empId, new Map());
}

const empResources = this.accessStore.get(empId)!;

if(!empResources.has(resId)) {
    empResources.set(resId, []);
}

const record: GrantRecord = {
    permission, 
    metadata: {
        grantedAt: Date.now(),
        grantedBy
    }
};

empResources.get(resId)!.push(record);
}

public revoke(empId: string, resId: string): void {
    
const empResources = this.accessStore.get(empId);

if(!empResources) {
    return;
}

empResources.delete(empId);

if(empResources.size === 0) {
    this.accessStore.delete(empId);
}
}

public getAllResourcesForEmployee(empId: string) {
    
const allResources = this.accessStore.get(empId);

if(!allResources) {
    return [];
}

const resourceList = [];

for(const entry of allResources.entries()) {
    
const resId = entry[0];
const grants = entry[1];

const latestGrant = grants[grants.length - 1];

resourceList.push({
    resId, 
    activePermission: latestGrant.permission,
    auditTrail: grants
})
}

return resourceList;

}

}



//part2: expiration + permission hierarchy

type Permission = 'read' | 'write' | 'admin';

interface GrantMetadata {
    grantedAt: number;
    grantedBy: string;
    source: string; 
    expiresAt?: number;
}

interface GrantRecord {
    permission: Permission;
    metadata: GrantMetadata;
}

interface GrantInfo extends GrantRecord {
    expired: boolean;
}

const PERMISSION_HIERARCHY: Record<Permission, Permission[]> = {
    admin: ['read', 'write', 'admin'],
    write: ['read', 'write'],
    read: ['read']
}


class AccessManager {
    
// emp-> resource-> grants
private accessStore: Map<string, Map<string, GrantRecord[]>> = new Map();

public grant(empId: string, resId: string, permission: Permission, metadata: GrantMetadata): void {
    
if(!this.accessStore.get(empId)) {
    this.accessStore.set(empId, new Map());
}

const empResources = this.accessStore.get(empId)!;

if(!empResources.has(resId)) {
    empResources.set(resId, []);
}

const record: GrantRecord = {
    permission, 
    metadata: {
        ...metadata
    }
};

empResources.get(resId)!.push(record);
}

public revoke(empId: string, resId: string): void {
    
const empResources = this.accessStore.get(empId);

if(!empResources) {
    return;
}

empResources.delete(resId);

if(empResources.size === 0) {
    this.accessStore.delete(empId);
}
}


public get(empId: string, resId: string) {
    
const empResources = this.accessStore.get(empId);
const records = empResources ? (empResources.get(resId) || []) : [];

const now = Date.now();
const provenance: GrantInfo[] = [];

const effectivePermission: Record<Permission, boolean> = {
    read: false,
    write: false,
    admin: false
}

for(const record of records) {
    
const isExpired = !!record.metadata.expiresAt && record.metadata.expiresAt < now;

provenance.push({
    ...record, 
    expired: isExpired
});

if(isExpired) continue;

const impliedPermission = PERMISSION_HIERARCHY[record.permission];

for(const perm of impliedPermission) {
    effectivePermission[perm] = true;
}
}

return {
    effectivePermission, 
    provenance
}
}

public getAllResourcesForEmployee(empId: string) {
    
const allResources = this.accessStore.get(empId);

if(!allResources) {
    return [];
}

const resourceList = [];

for(const entry of allResources.entries()) {
    
const resId = entry[0];
const grants = entry[1];

const latestGrant = grants[grants.length - 1];

resourceList.push({
    resId, 
    activePermission: latestGrant.permission,
    auditTrail: grants
})
}

return resourceList;

}

}


//Part3- Added the  effect followup. Final complete code with all the features 

type Permission = 'read' | 'write' | 'admin';
type Effect = 'allow' | 'deny';

interface GrantMetadata {
    grantedAt: number;
    grantedBy: string;
    source: string; 
    expiresAt?: number;
    effect?: Effect;
}

interface GrantRecord {
    permission: Permission;
    metadata: GrantMetadata;
}

interface GrantInfo extends GrantRecord {
    expired: boolean;
}

const PERMISSION_HIERARCHY: Record<Permission, Permission[]> = {
    admin: ['read', 'write', 'admin'],
    write: ['read', 'write'],
    read: ['read']
}

function getSourcePrecedence(source: string): number {
    if(source === 'admin') return 3;
    if(source.startsWith('role:')) return 2;
    if(source.startsWith('policy:')) return 1;  
    return 0;
}


class AccessManager {
    
// emp-> resource-> grants
private accessStore: Map<string, Map<string, GrantRecord[]>> = new Map();

public grant(empId: string, resId: string, permission: Permission, metadata: GrantMetadata): void {
    
if(!this.accessStore.get(empId)) {
    this.accessStore.set(empId, new Map());
}

const empResources = this.accessStore.get(empId)!;

if(!empResources.has(resId)) {
    empResources.set(resId, []);
}

const record: GrantRecord = {
    permission, 
    metadata: { 
        effect: 'allow', // default to allow when not specified
        ...metadata
    }
};

empResources.get(resId)!.push(record);
}


public revoke(empId: string, resId: string): void {
    
const empResources = this.accessStore.get(empId);

if(!empResources) {
    return;
}

empResources.delete(resId);

if(empResources.size === 0) {
    this.accessStore.delete(empId);
}
}

    //remove specific grants instead of removing the complete resource
    public targetedRevoke(empId: string, resId: string, permission: Permission, source: string): void {
        
    const empResources = this.accessStore.get(empId);
    if(!empResources) return;
    
    const grants = empResources.get(resId);
    if(!grants) return;
    
    const updatedGrants = grants.filter(record => 
    !(record.permission === permission && record.metadata.source === source)
)

if(updatedGrants.length === 0) {
    empResources.delete(resId);
}
else {
    empResources.set(resId, updatedGrants);
}

if(empResources.size === 0) {
    this.accessStore.delete(empId);
}


}


public get(empId: string, resId: string) {
    
const empResources = this.accessStore.get(empId);
const records = empResources ? (empResources.get(resId) || []) : [];

const now = Date.now();
const provenance: GrantInfo[] = [];

const effectiveRuleByPermission = new Map<Permission, {effect: Effect, rank: number}>();

for(const record of records) {
    
const isExpired = !!record.metadata.expiresAt && record.metadata.expiresAt < now;

provenance.push({
    ...record, 
    expired: isExpired
});

if(isExpired) continue;

const effect = record.metadata.effect || 'allow';
const rank = getSourcePrecedence(record.metadata.source);
const curr = effectiveRuleByPermission.get(record.permission);

if(!curr || rank > curr.rank || (rank === curr.rank && effect === 'deny')) {
    effectiveRuleByPermission.set(record.permission, {effect, rank});
}
}

const effectivePermission: Record<Permission, boolean> = {
    read: false,
    write: false,
    admin: false
}
// Hierarchy Expansion: Only apply implications for 'allow' rules that weren't overridden

for(const [perm, rule] of effectiveRuleByPermission.entries()) {
    if(rule.effect !== 'allow') continue;
    
    const impliedPermission = PERMISSION_HIERARCHY[perm];
    
    for(const impPerm of impliedPermission) {
        
    const impRule = effectiveRuleByPermission.get(impPerm);
    
    // Grant the implied permission unless it has a specific 'deny' rule overriding it
    
    if(!impRule || impRule.effect === 'allow') {
        effectivePermission[impPerm] = true;
    }
}
}

return {effectivePermission, provenance};
}

public getAllResourcesForEmployee(empId: string) {
    
const allResources = this.accessStore.get(empId);

if(!allResources) {
    return [];
}

const resourceList = [];

for(const resId of allResources.keys()) {
    
const access = this.get(empId, resId);

resourceList.push({
    resId,
    effectivePermission: access.effectivePermission,
    auditTrail: access.provenance
})
}

return resourceList;

}

}

*/